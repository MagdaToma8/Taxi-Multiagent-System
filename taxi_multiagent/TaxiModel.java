import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import jason.asSyntax.Literal;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;
import jason.stdlib.queue.add;

/** class that implements the Model of Taxi-Driver application */
public class TaxiModel extends GridWorldModel {

    // constants for the grid objects

    public static final int RED = 8;
    public static final int YELLOW = 16;
    public static final int BLUE = 32;
    public static final int GREEN = 64;
    public final int CLIENT = 2;

    // the grid size
    public static final int GSize = 5;

    int reward = 0; // total reward of actions
    ArrayList<Integer> total_rewards = new ArrayList<Integer>(); // reward earned in each episode

    int maxActions = 40; // max actions for every episode

    // Dictionary including clientId (key) and the pair of current and goal location
    // of client (value)
    Dictionary<Integer, Location[]> dictionary = new Hashtable<>();

    int clientID = 0; // the id of the client used in the dictionary

    int totalClients = 0; // total number of clients

    // ArrayLists representing the walls in the environment
    public ArrayList<Location> envWallLeft = new ArrayList<Location>();
    public ArrayList<Location> envWallRight = new ArrayList<Location>();
    public ArrayList<Location> envWallTop = new ArrayList<Location>();
    public ArrayList<Location> envWallBottom = new ArrayList<Location>();

    // Goal Locations
    Location lRed = new Location(0, 0);
    Location lYellow = new Location(0, 4);
    Location lBlue = new Location(3, 4);
    Location lGreen = new Location(4, 0);

    // Random location of agent
    Random rand = new Random();
    int randX = rand.nextInt(5);
    int randY = rand.nextInt(5);
    Location lTaxi1 = new Location(randX, randY);
    int randX1 = rand.nextInt(5);
    int randY1 = rand.nextInt(5);
    Location lTaxi2 = new Location(randX1, randY1);

    TaxiAgent[] agents = new TaxiAgent[getNbOfAgs()];

    public TaxiModel() {
        // create a 5x5 grid with one mobile agent
        super(GSize, GSize, 2);

        // initial location of taxi
        // ag code 0 means the taxi1 and code 1 means the taxi2
        setAgPos(0, lTaxi1);
        setAgPos(1, lTaxi2);

        for (int i = 0; i < getNbOfAgs(); i++) {
            agents[i] = new TaxiAgent();
        }

        // add goal locations on grid
        add(RED, lRed);
        add(YELLOW, lYellow);
        add(BLUE, lBlue);
        add(GREEN, lGreen);

        // initialize the walls in the environment
        for (int i = 0; i < 5; i++) {
            envWallLeft.add(new Location(0, i));
            envWallTop.add(new Location(i, 0));
            envWallBottom.add(new Location(i, 4));
            envWallRight.add(new Location(4, i));
        }

        envWallLeft.add(new Location(2, 0));
        envWallLeft.add(new Location(2, 1));
        envWallLeft.add(new Location(3, 3));
        envWallLeft.add(new Location(3, 4));
        envWallLeft.add(new Location(1, 3));
        envWallLeft.add(new Location(1, 4));

        envWallRight.add(new Location(1, 0));
        envWallRight.add(new Location(1, 1));
        envWallRight.add(new Location(0, 3));
        envWallRight.add(new Location(0, 4));
        envWallRight.add(new Location(2, 3));
        envWallRight.add(new Location(2, 4));

    }

    /**
     * Function generating clients at random location
     * <p>
     * The function generates the initial and the dropping location of the client in
     * one of the four
     * goal location ({@link #lBlue}, {@link #lYellow}, {@link #lRed},
     * {@link #lGreen})
     * 
     * @return the id of the client that generated
     */
    int generateRandomClients() {

        // Adding a client in one of the four goal locations
        Location[] locations = { lYellow, lBlue, lGreen, lRed };
        Random rand = new Random();
        int randLoc = rand.nextInt(4);
        int randLoc2 = rand.nextInt(4);
        // Add client to grid
        add(CLIENT, locations[randLoc]);
        Location currentLocation = locations[randLoc];
        Location dropLoc = locations[randLoc2];
        // Putting the pair of client's current and drop location on an array
        Location[] clientLocations = { currentLocation, dropLoc };
        System.out.println("[Env]: A client was generated at " + clientLocations[0] + ", " + clientLocations[1]);

        // Adding the elements to the dictionary
        dictionary.put(clientID, clientLocations);
        totalClients++;
        clientID++;

        return clientID - 1;
    }

    /**
     * Agent action allowing the agent to check for clients in grid
     * 
     * @return true
     */
    boolean checkForClient(String agent) {
        System.out.println("[" + agent + "]: Checking for clients...");
        return true;
    }

    /**
     * Agent action allowing it to set the variables necessary for the movement
     * 
     * @param id    the id of the client
     * @param at    the location of the client
     * @param drop  the location that the client wants to go
     * @param agent the name of the agent
     * 
     * @return true
     */
    boolean updateSelf(int id, Location at, Location drop, String agent) {
        // Get from the name the id of the taxi
        int agId = Integer.parseInt(agent.substring(4)) - 1;
        TaxiAgent ag = agents[agId];
        // Set the variables
        ag.setBestClient(id);
        ag.setClientLocation(at);
        ag.setGoalLocation(at);
        ag.setDropLocation(drop);
        return true;
    }

    /**
     * Agent action, helping it to calculate the distance of a given route
     * <p>
     * Utilizes the Manhattan distance between two points in the grid
     * 
     * @param at    the location of the client
     * @param drop  the location that the client wants to go
     * @param agent the name of the agent
     * 
     * @return true if the action is valid
     */
    boolean chooseClient(Location at, Location drop, String agent) {
        // Get from the name the id of the taxi
        int agId = Integer.parseInt(agent.substring(4)) - 1;
        System.out.println("[" + agent + "]: Calculating distance...");

        TaxiAgent ag = agents[agId];

        Location agPos = getAgPos(agId);

        int dist1 = 0;
        int dist2 = 0;
        // Create the ids of the two subroutes
        String id1 = new String(agPos + "_" + at);
        String id2 = new String(at + "_" + drop);
        // If the agent has found the best path for a subroute then the distance is the
        // size of the list
        if (ag.bestPath.containsKey(id1)) {
            dist1 = ag.bestPath.get(id1).size();
        }
        // Else calculate the Manhattan distance between agent's location and client's
        // location
        else {
            dist1 = agPos.distanceManhattan(at);
        }

        // In the same way as above find the distance of the second subroute
        if (ag.bestPath.containsKey(id2)) {
            dist2 = ag.bestPath.get(id2).size();
        } else {
            dist2 = at.distanceManhattan(drop);
        }

        // Add the two distances to get total distance of agent's route
        int totalDist = dist1 + dist2;
        System.out.println("[" + agent + "]: Total Dist of client is " + totalDist);
        agents[agId].setTotalDist(totalDist);

        return true;
    }

    /**
     * Agent action allowing the agent to move through the grid
     * <p>
     * Uses the function {@link #moveTo(String)}
     * 
     * @param dest  the location in which the agent wishes to go
     * @param agent the name of the agent
     * @return true if the action is valid
     */
    boolean moveTowards(Location dest, String agent) {
        // Get from the name, the id of the taxi
        int agId = Integer.parseInt(agent.substring(4)) - 1;
        TaxiAgent ag = agents[agId];
        // Get agent's location
        Location r1 = getAgPos(agId);

        String id = new String(r1 + "_" + dest);
        // If agent's bestPath has the above id and currently not writing a new path or
        // following
        // another path then follow the path of this id
        if (ag.bestPath.containsKey(id) && ag.hasBestPath() == false && ag.noEntry()) {
            ag.setHasBestPath(true);
            ag.setRouteId(id);
        }

        if (ag.hasBestPath()) {
            // Get the list of the locations
            ArrayList<Location> locs = ag.bestPath.get(ag.getRouteId());
            // Move on each of the locations in the list
            if (locs.size() != ag.getIdx() && locs.size() != 1) {
                Location location = locs.get(ag.getIdx());
                if (r1.x <= location.x && r1.x != location.x) {
                    r1 = moveTo("right", agent);
                } else if (r1.x >= location.x && r1.x != location.x) {
                    r1 = moveTo("left", agent);
                } else if (r1.y < location.y && r1.x == location.x) {
                    r1 = moveTo("down", agent);
                } else if (r1.y > location.y && r1.x == location.x) {
                    r1 = moveTo("up", agent);
                }
                int x = ag.getIdx();
                x++;
                ag.setIdx(x);
            } else {
                // Safety plan in case something is wrong
                ag.setHasBestPath(false);
                if (r1.x <= dest.x && r1.x != dest.x) {
                    r1 = moveTo("right", agent);
                } else if (r1.x >= dest.x && r1.x != dest.x) {
                    r1 = moveTo("left", agent);
                } else if (r1.y < dest.y && r1.x == dest.x) {
                    r1 = moveTo("down", agent);
                } else if (r1.y > dest.y && r1.x == dest.x) {
                    r1 = moveTo("up", agent);
                }

            }
        } else {
            // If not writing a new path
            if (ag.noEntry()) {
                // create an arrayList
                ArrayList<Location> newRoute = new ArrayList<Location>();
                // put a placeholder location
                newRoute.add(new Location(5, 5));
                // put a new entry to bestPath with key, the above id
                ag.bestPath.put(id, newRoute);
                ag.setRouteId(id);
                ag.setNoEntry(false);
            }
            // Everytime the agent should go to the same Column as the destination and then
            // to the same Row
            if (r1.x <= dest.x && r1.x != dest.x) {
                r1 = moveTo("right", agent);
            } else if (r1.x >= dest.x && r1.x != dest.x) {
                r1 = moveTo("left", agent);
            } else if (r1.y < dest.y && r1.x == dest.x) {
                r1 = moveTo("down", agent);
            } else if (r1.y > dest.y && r1.x == dest.x) {
                r1 = moveTo("up", agent);
            }
        }

        setAgPos(agId, r1); // move the agent in the grid

        return true;
    }

    /**
     * Supplementary function for {@link #moveTowards(Location)}
     * <p>
     * Indicates if the agent should move right, left, up or down
     * 
     * @param position the direction in which the agent will move. It takes the
     *                 values: up,
     *                 down, left or right
     * @param agent    the name of the agent
     * @return true if the action is valid
     */
    Location moveTo(String position, String agent) {
        // Get from the name, the id of the taxi
        int agId = Integer.parseInt(agent.substring(4)) - 1;
        TaxiAgent ag = agents[agId];
        // Get the location of the agent
        Location from = getAgPos(agId);
        Location from2 = getAgPos(agId);
        Location gLoc = ag.getGoalLocation();

        if (from.y == gLoc.y) {
            ag.setHasFoundY(true);
        }
        if (from.x == gLoc.x) {
            ag.setHasFoundX(true);
        }
        // The agent chooses up and the env is clear of walls
        if (position.equals("up") && !ag.wallAtTop.contains(from) && from.y != 0 && !envWallTop.contains(from)) {
            from.y--;
            // If the agent is in the same Row as the client then it has found the Row
            if (from.y == gLoc.y) {
                ag.setHasFoundY(true);
            }
            reward = reward - 1;
            System.out.println("[" + agent + "]: Going up, no wall...");
        }
        // The agent chooses up but there is a wall that it hasn't found
        else if (position.equals("up") && !ag.wallAtTop.contains(from) && envWallTop.contains(from)) {
            reward = reward - 100;
            // Add wall's location to every agent's 'beliefs'
            for (int i = 0; i < agents.length; i++) {
                agents[i].wallAtTop.add(from);
                from2.x = from.x;
                from2.y = from.y;
                from2.y--;
                // Add the other wall
                agents[i].wallAtBottom.add(from2);
            }
            System.out.println("[" + agent + "]: I bumped into a wall...");
        }
        // The agent chooses up, knows there is a wall there and hasn't found the Column
        // of the goal
        else if (position.equals("up") && ag.wallAtTop.contains(from) && !ag.hasFoundX()) {
            System.out.println("[" + agent + "]: There is a wall here...");
            // Depending on the goal's column move right or left
            if (from.x < gLoc.x) {
                System.out.println("[" + agent + "]: Going right");
                from = moveTo("right", agent);
            } else if (from.x > gLoc.x) {
                System.out.println("[" + agent + "]: Going left");
                from = moveTo("left", agent);
            }
        }
        // The agent chooses up, knows there is a wall there and has found the Column
        else if (position.equals("up") && ag.wallAtTop.contains(from) && ag.hasFoundX()) {
            // If the agent is at first column move only right
            if (from.x == 0) {
                ag.setGoTo("right");
                from = moveTo(ag.getGoTo(), agent);
                System.out.println("[" + agent + "]: I have found the column and I'm on edge so I go right");
            }
            // If the agent is at last column move only left
            else if (from.x == GSize - 1) {
                ag.setGoTo("left");
                from = moveTo(ag.getGoTo(), agent);
                System.out.println("[" + agent + "]: I have found the column and I'm on edge so I go left");
            }
            // The agent is free to move either left or right
            else if (ag.wallAtTop.contains(from)) {
                // If agent hasn't deicided where to go, choose left or right randomly and stick
                // to that
                if (ag.getGoTo().equals("nothing")) {
                    Random rand = new Random();
                    int coinFlip = rand.nextInt(2);
                    if (coinFlip == 0) {
                        ag.setGoTo("right");
                    } else {
                        ag.setGoTo("left");
                    }
                }
                System.out.println("[" + agent + ": I have found the column and I will go " + ag.getGoTo());
                from = moveTo(ag.getGoTo(), agent);
            }

        }
        // The agent chooses down and the env is clear of walls
        else if (position.equals("down") && !ag.wallAtBottom.contains(from) && !envWallBottom.contains(from)) {
            from.y++;
            // If the agent is in the same Row as the client then it has found the Row
            if (from.y == gLoc.y) {
                ag.setHasFoundY(true);
            }
            reward = reward - 1;
            System.out.println("[" + agent + "]: Going down, no wall...");
        }
        // The agent chooses down but there is a wall that it hasn't found
        else if (position.equals("down") && !ag.wallAtBottom.contains(from) && envWallBottom.contains(from)) {
            System.out.println("[" + agent + "]: I bumped into a wall...");
            reward = reward - 100;
            // Add wall's location to every 'beliefs'
            for (int i = 0; i < agents.length; i++) {
                agents[i].wallAtBottom.add(from);
                from2.x = from.x;
                from2.y = from.y;
                from2.y++;
                // Add the other wall
                agents[i].wallAtTop.add(from2);
            }

        }
        // The agent chooses down, knows there is a wall there and hasn't found the
        // Column of the goal
        else if (position.equals("down") && ag.wallAtBottom.contains(from) &&
                !ag.hasFoundX()) {
            System.out.println("[" + agent + "]: There is a wall here...");
            // Depending on the goal's column move right or left
            if (from.x < gLoc.x) {
                System.out.println("[" + agent + "]: Going right");
                from = moveTo("right", agent);
            } else if (from.x > gLoc.x) {
                System.out.println("[" + agent + "]: Going left");
                from = moveTo("left", agent);
            }
        }
        // The agent chooses down, knows there is a wall there and has found the Column
        else if (position.equals("down") && ag.wallAtBottom.contains(from) &&
                ag.hasFoundX()) {
            // If the agent is at first column move only right
            if (from.x == 0) {
                ag.setGoTo("right");
                from = moveTo(ag.getGoTo(), agent);
                System.out.println("[" + agent + "]: I have found the column and I'm on edge so I go right");
            }
            // If the agent is at fourth column move only left
            else if (from.x == GSize - 1) {
                ag.setGoTo("left");
                from = moveTo(ag.getGoTo(), agent);
                System.out.println("[" + agent + "]: I have found the column and I'm on edge so I go left");
            }
            // The agent is free to move either left or right
            else if (ag.wallAtBottom.contains(from)) {
                // If agent hasn't deicided where to go, choose left or right randomly and stick
                // to that
                if (ag.getGoTo().equals("nothing")) {
                    Random rand = new Random();
                    int coinFlip = rand.nextInt(2);
                    if (coinFlip == 0) {
                        ag.setGoTo("right");
                    } else {
                        ag.setGoTo("left");
                    }
                }
                System.out.println("[" + agent + "]: I have found the column and I will go " + ag.getGoTo());
                from = moveTo(ag.getGoTo(), agent);
            }

        }
        // The agent chooses right and the env is clear of walls
        else if (position.equals("right") && !ag.wallAtRight.contains(from) && !envWallRight.contains(from)) {
            from.x++;
            System.out.println("[" + agent + "]: Going right, no wall...");
            // If the agent is in the same Col as the client then it has found the Col
            if (from.x == gLoc.x) {
                ag.setHasFoundX(true);
            }
            reward = reward - 1;
        }
        // The agent chooses right but there is a wall that it hasn't found
        else if (position.equals("right") && !ag.wallAtRight.contains(from) && envWallRight.contains(from)) {
            System.out.println("[" + agent + "]: I bumped into a wall...");
            reward = reward - 100;
            // Add wall's location to every agent's 'beliefs'
            for (int i = 0; i < agents.length; i++) {
                agents[i].wallAtRight.add(from);
                from2.x = from.x;
                from2.x++;
                from2.y = from.y;
                // Add the other wall
                agents[i].wallAtLeft.add(from2);
            }
        }
        // The agent chooses right, knows there is a wall there and hasn't found the
        // Column of the goal
        else if (position.equals("right") && ag.wallAtRight.contains(from) &&
                !ag.hasFoundY()) {
            // Depending on the goal's row move up or down
            System.out.println("[" + agent + "]: There is a wall here...");
            if (from.y > gLoc.y) {
                System.out.println("[" + agent + "]: Going up");
                from = moveTo("up", agent);
            } else if (from.y < gLoc.y) {
                System.out.println("[" + agent + "]: Going down");
                from = moveTo("down", agent);
            }
        }
        // The agent chooses right, knows there is a wall there and has found the Row
        else if (position.equals("right") && ag.wallAtRight.contains(from) &&
                ag.hasFoundY()) {
            // If the agent is at first row move only down
            if (from.y == 0) {
                System.out.println("[" + agent + "]: I have found row and I'm on edge so I go down");
                ag.setGoTo("down");
                from = moveTo(ag.getGoTo(), agent);
            }
            // If the agent is at fourth row move only up
            else if (from.y == GSize - 1) {
                System.out.println("[" + agent + "]: I have found the row and I'm on edge so I go up");
                ag.setGoTo("up");
                from = moveTo(ag.getGoTo(), agent);
            }
            // The agent is free to move either left or right
            else if (ag.wallAtRight.contains(from)) {
                // If agent hasn't deicided where to go, choose up or down randomly and stick to
                // that
                if (ag.getGoTo().equals("nothing")) {
                    Random rand = new Random();
                    int coinFlip = rand.nextInt(2);
                    if (coinFlip == 0) {
                        ag.setGoTo("up");
                    } else {
                        ag.setGoTo("down");
                    }
                }
                System.out.println("[" + agent + "]: I have found row and I will go " + ag.getGoTo());
                from = moveTo(ag.getGoTo(), agent);
            }
        }
        // The agent chooses left and the env is clear of walls
        else if (position.equals("left") && !ag.wallAtLeft.contains(from) && !envWallLeft.contains(from)) {
            from.x--;
            System.out.println("[" + agent + "]: Going left, no wall...");
            // If the agent is in the same Col as the client then it has found the Col
            if (from.x == gLoc.x) {
                ag.setHasFoundX(true);
            }

            reward = reward - 1;
        }
        // The agent chooses left but there is a wall that it hasn't found
        else if (position.equals("left") && !ag.wallAtLeft.contains(from) && envWallLeft.contains(from)) {
            reward = reward - 100;
            System.out.println("[" + agent + "]: I bumped into a wall...");
            // Add wall's location to every agent's 'beliefs'
            for (int i = 0; i < agents.length; i++) {
                agents[i].wallAtLeft.add(from);
                from2.x = from.x;
                from2.x--;
                from2.y = from.y;
                // Add the other wall
                agents[i].wallAtRight.add(from2);
            }

        }
        // The agent chooses left, knows there is a wall there and hasn't found the
        // Column of the goal
        else if (position.equals("left") && ag.wallAtLeft.contains(from) && !ag.hasFoundY()) {
            System.out.println("[" + agent + "]: There is a wall here...");
            // Depending on the goal's row move up or down
            if (from.y > gLoc.y) {
                System.out.println("[" + agent + "]: Going up");
                from = moveTo("up", agent);
            } else if (from.y < gLoc.y) {
                System.out.println("[" + agent + "]: Going down");
                from = moveTo("down", agent);
            }
        }
        // The agent chooses left, knows there is a wall there and has found the Row
        else if (position.equals("left") && ag.wallAtLeft.contains(from) && ag.hasFoundY()) {
            // If the agent is at first row move only down
            if (from.y == 0) {
                System.out.println("[" + agent + "]: I have found the row and I'm on edge so I go down");
                ag.setGoTo("down");
                from = moveTo(ag.getGoTo(), agent);
            }
            // If the agent is at fourth row move only up
            else if (from.y == GSize - 1) {
                System.out.println("[" + agent + "]: I have found the row and I'm on edge so I go up");
                ag.setGoTo("up");
                from = moveTo(ag.getGoTo(), agent);
            }
            // The agent is free to move either left or right
            else if (ag.wallAtLeft.contains(from)) {
                // If agent hasn't deicided where to go, choose up or down randomly and stick to
                // that
                if (ag.getGoTo().equals("nothing")) {
                    Random rand = new Random();
                    int coinFlip = rand.nextInt(2);
                    if (coinFlip == 0) {
                        ag.setGoTo("up");
                    } else {
                        ag.setGoTo("down");
                    }
                }
                System.out.println("[" + agent + "]: I have found the row and I will go " + ag.getGoTo());
                from = moveTo(ag.getGoTo(), agent);
            }
        }

        // If the agent is not following a path that already has found
        if (ag.hasBestPath() == false) {
            // Get the list of the routeId
            ArrayList<Location> arr = ag.bestPath.get(ag.getRouteId());
            // if the size is 1 then set finished to false
            if (arr.size() == 1) {
                ag.setFinished(false);
            }
            // If the list already contains this location
            if (arr.contains(from)) {
                // add it to the list
                arr.add(from);
                boolean hasDuplicate = true;
                int idx1 = arr.lastIndexOf(from);
                // remove all the locations from the list until you find the same location
                // (from)
                while (hasDuplicate) {
                    arr.remove(idx1);
                    idx1--;
                    Location loc = arr.get(idx1);
                    if (!loc.equals(from)) {
                        hasDuplicate = true;
                    } else {
                        hasDuplicate = false;
                    }
                }
            } else {
                String[] id = ag.getRouteId().split("_");
                // if the current location isn't the starting location or the edges of the
                // border, add it to the list
                if (!(id[0].equals(from.toString()) && (from.y == GSize - 1 || from.y == 0))) {
                    arr.add(from);
                }
                // if the current location is the goal location then set finished to true
                if (arr.contains(gLoc)) {
                    ag.setFinished(true);
                }
            }
            Location placeholderLoc = new Location(5, 5);
            // remove the placeholder location
            if (arr.contains(placeholderLoc)) {
                arr.remove(placeholderLoc);
            }
        }

        maxActions--;
        return from;
    }

    /**
     * Agent action "loading" a client to the taxi.
     * 
     * @param clientLoc the location in which the agent is doing the action
     * @param agent     the name of the agent
     * @return true if the action is valid, otherwise false
     */
    boolean loadClient(Location clientLoc, String agent) {
        // Get from the name, the id of the agent
        int agId = Integer.parseInt(agent.substring(4)) - 1;
        TaxiAgent ag = agents[agId];
        Location agLoc = getAgPos(agId);
        System.out.println("[" + agent + "]: I loaded the client!");

        // Agent must be available and at the same position as the client
        if (ag.isAvailable() && agLoc.equals(clientLoc)) {
            ag.setAvailable(false); // agent is no longer available
            reward = reward - 1;
            maxActions--;
            // re-initialize variables that are used in moveTo
            ag.setHasFoundY(false);
            ag.setHasFoundX(false);
            ag.setGoTo("nothing");
            Location goal = ag.getDropLocation();
            ag.setGoalLocation(goal);
            ag.setNoEntry(true);
            ag.setHasBestPath(false);
            ag.setRouteId("");
            ag.setIdx(0);
            remove(CLIENT, agLoc); // remove client from grid
            return true;
        } else {
            reward = reward - 10;
            maxActions--;
            return false;
        }
    }

    /**
     * Agent action "unloading" a client from taxi.
     * 
     * @param destLoc the location in which the agent is doing the action
     * @param agent   the name of the agent
     * @return true if the action is valid, otherwise false
     */
    boolean unloadClient(Location destLoc, String agent) {
        // Get from the name, the id of the agent
        int agId = Integer.parseInt(agent.substring(4)) - 1;
        TaxiAgent ag = agents[agId];
        Location agLoc = getAgPos(agId);
        System.out.println("[" + agent + "]: I unloaded the client!");

        // Agent must not be available and at the same position as the client
        if (!ag.isAvailable() && agLoc.equals(destLoc)) {
            ag.setAvailable(true);
            reward = reward + 20;
            maxActions--;
            // re-initialize variables that are used in moveTo
            ag.setHasFoundY(false);
            ag.setHasFoundX(false);
            ag.setGoTo("nothing");
            ag.setNoEntry(true);
            ag.setHasBestPath(false);
            ag.setClientLocation(ag.getStartingLocation());
            ag.setDropLocation(ag.getStartingLocation());
            ag.clPrices.remove(ag.getBestClient());
            ag.setRouteId("");
            ag.setIdx(0);
            dictionary.remove(ag.getBestClient()); // remove client from dictionary
            totalClients--;
            return true;
        } else {
            reward = reward - 10;
            maxActions--;
            return false;
        }
    }
}
