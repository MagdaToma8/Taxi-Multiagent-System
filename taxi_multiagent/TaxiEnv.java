import jason.NoValueException;
import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.Location;
import jason.stdlib.intend;

import java.sql.ClientInfoStatus;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class TaxiEnv extends Environment {

    // Literals that represent the beliefs of the agent

    public static Literal agentAt = Literal.parseLiteral("at(taxi,0,0)"); // Indicates the location of the taxi
    public static Literal clientAt = Literal.parseLiteral("at(client,4,0)"); // Indicates the location of the client
    public static Literal dropTo = Literal.parseLiteral("drop(client,0,4)"); // Indicates the location in which the
    // client wants to go
    public static Literal serving = Literal.parseLiteral("serving(taxi)"); // Indicates if the taxi is serving a client
    public static Literal reached = Literal.parseLiteral("hasReached(maxActions)"); // Indicates if the agent has
                                                                                    // reached his maximum actions per
                                                                                    // episode
    public static Literal cnp = Literal.parseLiteral("cnp(1,at(client,0,0),drop(client,0,4))");
    public static Literal price = Literal.parseLiteral("price(0,0)");
    public static Literal available = Literal.parseLiteral("isAvailable(taxi)");

    // Wall locations
    public static ArrayList<Location> wallsLeft = new ArrayList<Location>();
    public static ArrayList<Location> wallsRight = new ArrayList<Location>();
    public static ArrayList<Location> wallsTop = new ArrayList<Location>();
    public static ArrayList<Location> wallsBottom = new ArrayList<Location>();

    // List of the clients that a cnp has been assigned
    public static ArrayList<Integer> clientsServed = new ArrayList<Integer>();
    // List of clients waiting to be served
    public static Queue<Integer> clientsWaiting = new LinkedList<Integer>();

    public static boolean ag1 = false; // indicates if taxi1 has taken over a cnp
    public static boolean ag2 = false; // indicates if taxi2 has taken over a cnp
    public static boolean start = true; // used to prevent two clients to be generated at the same time

    static Logger logger = Logger.getLogger(TaxiEnv.class.getName());
    public Random rand = new Random();
    TaxiModel model; // the model of the grid

    @Override
    public void init(String[] args) {
        model = new TaxiModel();

        if (args.length == 1 && args[0].equals("gui")) {
            TaxiView view = new TaxiView(model);
            model.setView(view);
        }

        // for each agent in the grid add it's location as percept
        for (int i = 0; i < model.getNbOfAgs(); i++) {
            Location lTaxi = model.getAgPos(i);
            agentAt = Literal.parseLiteral("at(taxi," + lTaxi + ")");
            String agentName = new String("taxi" + (i + 1));
            addPercept(agentName, agentAt);
        }

    }

    @Override
    public boolean executeAction(String ag, Structure action) {
        System.out.println("[" + ag + "] doing: " + action);
        // Get from the name, the id of the agent
        int agId = Integer.parseInt(ag.substring(4)) - 1;
        boolean result = false;

        try {
            // The agent chose to do the action moveTowards
            if (action.getFunctor().equals("moveTowards")) {
                // Convert the action's arguments into a Location
                int x = (int) ((NumberTerm) action.getTerm(0)).solve();
                int y = (int) ((NumberTerm) action.getTerm(1)).solve();
                Location dest = new Location(x, y);
                // Do the action
                result = model.moveTowards(dest, ag);
            }
            // The agent chose to do the action checkForClient
            else if (action.getFunctor().equals("checkForClient")) {
                // Do the action
                result = model.checkForClient(ag);
            }
            // The agent chose to do the action updateSelf
            else if (action.getFunctor().equals("updateSelf")) {
                // Convert the terms into integers and strings
                int clientID = (int) ((NumberTerm) action.getTerm(0)).solve();
                String[] at = action.getTerm(1).toString().split(",");
                char a = at[2].charAt(0);
                Location atLoc = new Location(Integer.parseInt(at[1]), Character.getNumericValue(a));
                String[] drop = action.getTerm(2).toString().split(",");
                a = drop[2].charAt(0);
                Location dropLoc = new Location(Integer.parseInt(drop[1]), Character.getNumericValue(a));
                // Do the action
                result = model.updateSelf(clientID, atLoc, dropLoc, ag);
                ag1 = false;
                ag2 = false;
            }
            // The agent chose to do the action chooseClient
            else if (action.getFunctor().equals("chooseClient")) {
                // Convert the terms into integers and strings
                int clientID = (int) ((NumberTerm) action.getTerm(0)).solve();
                String[] at = action.getTerm(1).toString().split(",");
                char a = at[2].charAt(0);
                Location atLoc = new Location(Integer.parseInt(at[1]), Character.getNumericValue(a));
                String[] drop = action.getTerm(2).toString().split(",");
                a = drop[2].charAt(0);
                Location dropLoc = new Location(Integer.parseInt(drop[1]), Character.getNumericValue(a));
                // Do the action
                result = model.chooseClient(atLoc, dropLoc, ag);
                // Add the total distance as a price percept
                price = Literal.parseLiteral("price(" + clientID + "," + model.agents[agId].getTotalDist() + ")");
                addPercept(ag, price);
                // Save the total distance in the clPrices at the index of clientId
                model.agents[agId].clPrices.set(clientID, model.agents[agId].getTotalDist());
            }
            // The agent chose to do the action loadClient
            else if (action.getFunctor().equals("loadClient")) {
                // Convert the action's arguments into a Location
                String x = action.getTerm(0).toString();
                String y = action.getTerm(1).toString();
                Location loc = new Location(Integer.parseInt(x), Integer.parseInt(y));
                // Do the action
                result = model.loadClient(loc, ag);

            }
            // The agent chose to do the action unloadClient
            else if (action.getFunctor().equals("unloadClient")) {
                // Convert the action's arguments into a Location
                String x = action.getTerm(0).toString();
                String y = action.getTerm(1).toString();
                Location loc = new Location(Integer.parseInt(x), Integer.parseInt(y));
                // Do the action
                result = model.unloadClient(loc, ag);
            } else {
                logger.info("Failed to execute action " + action);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Total reward is: " + model.reward);
        updatePercepts(ag);

        try {
            // Generate a client only when a boolean condition is true and the number of
            // clients is less than to 4
            //
            if (rand.nextBoolean() && model.totalClients < 4 && start) {
                start = false;
                int id = model.generateRandomClients();
                clientsWaiting.add(id);
                start = true;
            }
            // Wait for 3 seconds.
            Thread.sleep(3000);
        } catch (Exception e) {
        }
        return result;
    }

    /**
     * Function called when an episode is over. Most of the variables on the
     * TaxiAgnet and TaxiModel are re-initialized
     */
    void terminateEpisode() {
        System.out.println("[Env]: Game Over!");
        System.out.println("[Env]: Starting Again...");
        for (int i = 0; i < model.agents.length; i++) {

            model.agents[i].setAvailable(true);
            model.agents[i].setHasFoundY(false);
            model.agents[i].setHasFoundX(false);
            model.agents[i].setChoose("nothing");
            model.agents[i].setGoTo("nothing");

            Random rand = new Random();
            int randX = rand.nextInt(5);
            int randY = rand.nextInt(5);
            model.setAgPos(i, randX, randY);
            Location startL = model.agents[i].getStartingLocation();
            model.agents[i].setClientLocation(startL);
            model.agents[i].setDropLocation(startL);
            model.agents[i].setNoEntry(true);
            model.agents[i].setHasBestPath(false);

            if (!model.agents[i].isFinished()) {
                Entry<String, ArrayList<Location>> lastElement = null;
                Iterator<Entry<String, ArrayList<Location>>> iter = model.agents[i].bestPath.entrySet().iterator();
                while (iter.hasNext()) {
                    lastElement = iter.next();
                }
                model.agents[i].bestPath.remove(lastElement.getKey());
            }

            model.agents[i].setFinished(false);
            model.agents[i].setRouteId("");
            model.agents[i].setIdx(0);
            model.agents[i].clPrices.clear();
            for (int j = 0; j < 20; j++) {
                model.agents[i].clPrices.add(null);
            }
        }
        Enumeration<Integer> keys = model.dictionary.keys();
        while (keys.hasMoreElements()) {
            int key = keys.nextElement();
            Location[] locs = model.dictionary.get(key);
            model.remove(model.CLIENT, locs[0]);
        }
        Dictionary<Integer, Location[]> dictionary = new Hashtable<>();
        model.dictionary = dictionary;
        model.clientID = 0;
        model.totalClients = 0;
        model.maxActions = 40;
        model.total_rewards.add(model.reward);
        System.out.println("Rewards on each episode: " + model.total_rewards);
        System.out.println();
        model.reward = 0;
        ag1 = false;
        ag2 = false;
        start = true;
        clientsWaiting.clear();
        clientsServed.clear();
    }

    /** creates the agents percepts based on the TaxiModel */
    void updatePercepts(String agent) {
        // clear the percepts of the agent
        clearPercepts(agent);
        // if the episode is over, add the belief hasReached(maxActions) to all agents
        // and call the
        // function terminateEpisode()
        if (model.maxActions <= 0) {
            for (int i = 0; i < model.getNbOfAgs(); i++) {
                String agentName = new String("taxi" + (i + 1));
                addPercept(agentName, reached);
            }
            terminateEpisode();
        }

        // Get the id of the agent from it's name
        int agId = Integer.parseInt(agent.substring(4)) - 1;

        // Add the taxi's location to the agent's beliefs

        Location lTaxi = model.getAgPos(agId);
        agentAt = Literal.parseLiteral("at(taxi," + lTaxi + ")");
        addPercept(agent, agentAt);
        System.out.println("[" + agent + "]: I am at " + lTaxi);

        // Get client and drop location
        Location clientLoc = model.agents[agId].getClientLocation();
        Location dropLoc = model.agents[agId].getDropLocation();
        Location startingLocation = new Location(2, 2);

        // If the above locations are 2,2 it means that still the agent isnt't serving a
        // client
        if (!clientLoc.equals(startingLocation) && !dropLoc.equals(startingLocation)) {
            // Add the client's location and dropping location to the agent's beliefs
            Literal clientAt1 = Literal.parseLiteral("at(client," + clientLoc + ")");
            Literal dropTo1 = Literal.parseLiteral("drop(client," + dropLoc + ")");
            addPercept(agent, clientAt1);
            addPercept(agent, dropTo1);
            System.out.println("[" + agent + "]: The client I am serving is at " + clientLoc +
                    " and wants to go to " + dropLoc);
            System.out.println();
        }

        // If there is more than one client on the gird
        if (model.totalClients != 0) {
            // Add all the offers (prices) that the agent has done to its percepts
            ArrayList<Integer> prices = model.agents[agId].clPrices;
            for (int i = 0; i < prices.size(); i++) {
                if (prices.get(i) != null) {
                    price = Literal.parseLiteral("price(" + i + "," + prices.get(i) + ")");
                    addPercept(agent, price);
                }
            }

            // If none of the agents are doing a cnp and there is at least one client
            if (ag1 == false && ag2 == false && clientsWaiting.size() != 0) {
                // Assign a cnp with the clients details to the agent
                int key = clientsWaiting.remove();
                if (!clientsServed.contains(key)) {

                    Location[] clientLocs = model.dictionary.get(key);
                    Location clientLoc2 = clientLocs[0];
                    Location dropLoc2 = clientLocs[1];

                    Literal clientAt = Literal.parseLiteral("at(client," + clientLoc2 + ")");
                    Literal dropTo = Literal.parseLiteral("drop(client," + dropLoc2 + ")");
                    Literal cnp = Literal.parseLiteral("cnp(" + key + "," + clientAt + "," + dropTo + ")");
                    addPercept(agent, cnp);

                    // Add the id of the client in the list
                    clientsServed.add(key);
                    ag1 = containsPercept("taxi1", cnp);
                    ag2 = containsPercept("taxi2", cnp);
                }
            }
        }
    }
}
