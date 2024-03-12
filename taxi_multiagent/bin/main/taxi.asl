//Agent Knowledge
isAvailable(taxi).

all_proposals_received(CNPId,NP) :-  
     .count(propose(CNPId,_)[source(_)], NO) &  
     .count(refuse(CNPId)[source(_)], NR) &    
     NP = NO + NR.  // num(participants) = num(proposals) + num(refusals)

/* Plans */
!check.

//remove the belief doingCNP
+notDoingCNP[source(_)]
   <- -doingCNP[source(_)]; 
      -notDoingCNP[source(_)].

//The agent checks for clients doing the action checkForClient and continues to check recursively
+!check : not cnp(Id,At,Drop) & not serving(taxi)
     <-  checkForClient;
         !check.
+!check.

//If the agent gets a cnp then achieve the goal initiateCNP
+cnp(Id,At,Drop)[source(percept)]
   <- !initiateCNP(Id,At,Drop).

//Defines the process to initiate a cnp
+!initiateCNP(Id,At,Drop): not doingCNP
   <- +doingCNP; //add doingCNP to beliefs
      +cnp(Id,At,Drop); //add the cnp to beliefs
      !call(Id,At,Drop,LP); //call for bids
      !bid(Id,LP); //wait for bids to arrive
      !winner(Id,LO,WAg); //determine the winner
      !result(Id,LO,WAg,At,Drop); //announce the results
      -cnp(Id,At,Drop). //remove cnp from beliefs

//In case another cnp is in action, then wait and try again
+!initiateCNP(Id,At,Drop): doingCNP
   <- .print("A CNP is already in action"); 
      .wait(4000);
      !initiateCNP(Id,At,Drop).

//Defines the process to call for bids
+!call(Id,At,Drop,LP)
   <- .print("Waiting taxi drivers for ",At,", ",Drop,"...");  
      .all_names(LP); //get a list of all agents
      .print("Sending CFP to, ", LP);
      .broadcast(tell,doingCNP); //broadcast to all agents that a cnp is in action
      .send(LP,tell,cfp(Id,At,Drop)). //send to all agents a call for proposals

+!bid(Id,LP) // the deadline of the CNP is now + 10 seconds (or all proposals received)
   <- .print("Now we wait ",LP);
      .wait(all_proposals_received(Id,.length(LP)), 10000, _).

+!winner(Id,LO,WAg) : .findall(offer(O,A),propose(Id,O)[source(A)],LO) & LO \== []
   <- .print("Offers are ",LO);
      .min(LO,offer(WOf,WAg)); // the first offer is the best
      .print("Winner is ",WAg," with ",WOf).
+!winner(Id,_,nowinner)  // no offer case
   <- .print("No offers, I'll wait.");
      +notDoingCNP[source(self)];
      .broadcast(tell,notDoingCNP);
      -propose(CNPId,_)[source(_)];
      -refuse(CNPId)[source(_),source(self)];
      .wait(8000); //wait and try again
      ?cnp(Id,At,Drop);
      !initiateCNP(Id,At,Drop).

+!result(_,[],_,_,_).
+!result(CNPId,[offer(_,WAg)|T],WAg,At,Drop) // announce result to the winner
   <- +notDoingCNP;
      .broadcast(tell,notDoingCNP); //tell all agents that the cnp is over
      .send(WAg,tell,accept_proposal(CNPId,At,Drop));
      !result(CNPId,T,WAg,At,Drop).
+!result(CNPId,[offer(_,LAg)|T],WAg,At,Drop) // announce to others
   <- .send(LAg,tell,reject_proposal(CNPId,At,Drop));
      .send(LAg,tell,notDoingCNP);
      !result(CNPId,T,WAg,At,Drop).
      

//If a call for proposal is added, make an offer and send it to bidder
+cfp(CNPId,At,Drop)[source(A)]: isAvailable(taxi)
    <- !makeOffer(CNPId,At,Drop);
       ?price(CNPId, Offer);
       +proposal(CNPId,At,Drop,Offer); // remember my proposal
       .send(A,tell,propose(CNPId,Offer)).

//The agent is serving a client so it refuses the cfp
+cfp(CNPId,At,Drop)[source(A)]: not isAvailable(taxi) 
    <- .send(A,tell,refuse(CNPId));
       -cfp(CNPId,At,Drop)[source(A)].

+!makeOffer(CNPId,At,Drop): isAvailable(taxi)
   <- chooseClient(CNPId,At,Drop). //calculate the total distance of route

//The agent won the proposal so it will serve the client
@r1 +accept_proposal(CNPId,At,Drop) : proposal(CNPId,At,Drop,Offer)
   <- -isAvailable(taxi);
      .print("My proposal '",Offer,"' won CNP ",CNPId, " for ",At,Drop,"!");
      updateSelf(CNPId,At,Drop); //update its variables
      -cnp(CNPId,At,Drop);
      +serving(taxi).

//The agent's offer got rejected
@r2 +reject_proposal(CNPId,At,Drop)
   <- .print("I lost CNP ",CNPId, ".");
      -proposal(CNPId,At,Drop);
      -cnp(CNPId,At,Drop).

// The agent won a cnp so it initiates the action to serve the client
+serving(taxi) <- !serve(client).

// Defines the process to serve a client
+!serve(client) //: serving(taxi,At,Drop)
   <- ?at(client,C,R); // find out the location of the chose client   
      !at(taxi,C,R); //go to that location
      loadClient(C,R); //load the client on that location
      ?drop(client,C1,R1); // find out the location in which the client wants to go
      !at(taxi,C1,R1);   //go to that location
      unloadClient(C1,R1); //unload the client on that location
      +isAvailable(taxi);
      -serving(taxi).

// +!serve(client) <- !check.


// Manages the movement of taxi toward a specific location (C,R).
+!at(taxi,C,R) : at(taxi,C,R).
+!at(taxi,C,R)
    <- moveTowards(C,R); // Repeatedly moving one step towards the right location.
       !at(taxi,C,R).

// If agent has this belief, it drops all of his desires and intentions and removes all the beliefs asociated with the cnps in order to start again
+hasReached(maxActions) : true
   <- .print("I have reached max Actions"); 
      .drop_all_intentions;
      .drop_all_events;
      .drop_all_desires; 
      -serving(taxi);
      .abolish(cnp(_,_,_));
      .abolish(cfp(_,_,_));
      .abolish(reject_proposal(_,_,_));
      .abolish(accept_proposal(_,_,_));
      .abolish(proposal(_,_,_,_));
      .abolish(propose(_,_));
      .abolish(refuse(_));
      +isAvailable(taxi);
      +notDoingCNP;
      !check.
