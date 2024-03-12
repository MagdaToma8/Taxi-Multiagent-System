import java.util.ArrayList;
import java.util.LinkedHashMap;

import jason.environment.grid.Location;

public class TaxiAgent {

    private boolean isAvailable = true; // if taxi is free to pick up a client
    private boolean hasFoundY = false; // indicate if agent has been at the same Column as the client
    private boolean hasFoundX = false; // indicate if agent has been at the same Row as the client
    private String choose = "nothing"; // takes position values depending on a coin flip
    private String goTo = "nothing"; // takes position values depending on goal location
    private boolean hasBestPath = false; // indicates if the agent has found the best path for a route
    private boolean noEntry = true; // indicates if the agent has created an entry in the bestPath HashMap
    private String routeId = ""; // the id of the route that the agent is doing
    private int idx = 0; // the index of the last entry in the ArrayList in bestPath
    private boolean finished = false; // indicates if an entry in the ArrayList in bestPath is completed
    private int totalDist = 0; // total distance of a route
    private int bestClient = -1; // the chosen client
    private Location startingLocation = new Location(2, 2);
    private Location goalLocation = startingLocation; // Current agent's goal location
    private Location clientLocation = startingLocation; // Current clent's location
    private Location dropLocation = startingLocation; // Current client's drop location

    // ArrayLists representing the walls in the environment
    public ArrayList<Location> wallAtLeft = new ArrayList<Location>();
    public ArrayList<Location> wallAtRight = new ArrayList<Location>();
    public ArrayList<Location> wallAtTop = new ArrayList<Location>();
    public ArrayList<Location> wallAtBottom = new ArrayList<Location>();

    // A HasMap that stores the best path (list of locations) for a given id
    // (startingLocation_lastLocation).
    public LinkedHashMap<String, ArrayList<Location>> bestPath = new LinkedHashMap<String, ArrayList<Location>>();

    // An ArrayList that stores the offers that the agent has made
    public ArrayList<Integer> clPrices = new ArrayList<Integer>();

    public TaxiAgent() {
        super();
        // Initiate the list with 10 null values
        for (int i = 0; i < 20; i++) {
            this.clPrices.add(null);
        }
    }

    // Setters - Getters
    public int getBestClient() {
        return bestClient;
    }

    public void setBestClient(int bestClient) {
        this.bestClient = bestClient;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public boolean hasFoundY() {
        return hasFoundY;
    }

    public void setHasFoundY(boolean hasFoundY) {
        this.hasFoundY = hasFoundY;
    }

    public boolean hasFoundX() {
        return hasFoundX;
    }

    public void setHasFoundX(boolean hasFoundX) {
        this.hasFoundX = hasFoundX;
    }

    public String getChoose() {
        return choose;
    }

    public void setChoose(String choose) {
        this.choose = choose;
    }

    public String getGoTo() {
        return goTo;
    }

    public void setGoTo(String goTo) {
        this.goTo = goTo;
    }

    public boolean hasBestPath() {
        return hasBestPath;
    }

    public void setHasBestPath(boolean hasBestPath) {
        this.hasBestPath = hasBestPath;
    }

    public boolean noEntry() {
        return noEntry;
    }

    public void setNoEntry(boolean noEntry) {
        this.noEntry = noEntry;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int getTotalDist() {
        return totalDist;
    }

    public void setTotalDist(int totalDist) {
        this.totalDist = totalDist;
    }

    public Location getGoalLocation() {
        return goalLocation;
    }

    public Location getStartingLocation() {
        return startingLocation;
    }

    public void setStartingLocation(Location startingLocation) {
        this.startingLocation = startingLocation;
    }

    public void setGoalLocation(Location goalLocation) {
        this.goalLocation = goalLocation;
    }

    public Location getClientLocation() {
        return clientLocation;
    }

    public void setClientLocation(Location clientLocation) {
        this.clientLocation = clientLocation;
    }

    public Location getDropLocation() {
        return dropLocation;
    }

    public void setDropLocation(Location dropLocation) {
        this.dropLocation = dropLocation;
    }

}
