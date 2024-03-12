import jason.environment.grid.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/** class that implements the View of Domestic Robot application */
public class TaxiView extends GridWorldView {

    TaxiModel tmodel;

    public TaxiView(TaxiModel model) {
        super(model, "Taxi Agent", 700);
        tmodel = model;
        defaultFont = new Font("Arial", Font.BOLD, 16); // change default font
        setVisible(true);
        repaint();
    }

    /** draw application objects */
    @Override
    public void draw(Graphics g, int x, int y, int object) {
        Location lRobot1 = tmodel.getAgPos(0);
        Location lRobot2 = tmodel.getAgPos(1);
        int client = tmodel.CLIENT;
        super.drawAgent(g, x, y, Color.lightGray, -1);
        switch (object) {
            case TaxiModel.YELLOW:
                if (lRobot1.equals(tmodel.lYellow) || lRobot2.equals(tmodel.lYellow)) {
                    super.drawAgent(g, x, y, Color.pink, -1);
                } else if (tmodel.isFree(client, tmodel.lYellow)) {
                    super.drawAgent(g, x, y, Color.gray, -1);
                } else {
                    super.drawAgent(g, x, y, Color.yellow, -1);
                    g.setColor(Color.black);
                    drawString(g, x, y, defaultFont, "Client here");
                    break;
                }
                g.setColor(Color.black);
                drawString(g, x, y, defaultFont, "Yellow");
                break;

            case TaxiModel.BLUE:
                if (lRobot1.equals(tmodel.lBlue) || lRobot2.equals(tmodel.lBlue)) {
                    super.drawAgent(g, x, y, Color.pink, -1);
                } else if (tmodel.isFree(client, tmodel.lBlue)) {
                    super.drawAgent(g, x, y, Color.gray, -1);
                } else {
                    super.drawAgent(g, x, y, Color.blue, -1);
                    g.setColor(Color.black);
                    drawString(g, x, y, defaultFont, "Client here");
                    break;
                }
                g.setColor(Color.black);
                drawString(g, x, y, defaultFont, "Blue");
                break;

            case TaxiModel.RED:
                if (lRobot1.equals(tmodel.lRed) || lRobot2.equals(tmodel.lRed)) {
                    super.drawAgent(g, x, y, Color.pink, -1);
                } else if (tmodel.isFree(client, tmodel.lRed)) {
                    super.drawAgent(g, x, y, Color.gray, -1);
                } else {
                    super.drawAgent(g, x, y, Color.red, -1);
                    g.setColor(Color.black);
                    drawString(g, x, y, defaultFont, "Client here");
                    break;
                }
                g.setColor(Color.black);
                drawString(g, x, y, defaultFont, "Red");
                break;

            case TaxiModel.GREEN:
                if (lRobot1.equals(tmodel.lGreen) || lRobot2.equals(tmodel.lGreen)) {
                    super.drawAgent(g, x, y, Color.pink, -1);
                } else if (tmodel.isFree(client, tmodel.lGreen)) {
                    super.drawAgent(g, x, y, Color.gray, -1);
                } else {
                    super.drawAgent(g, x, y, Color.green, -1);
                    g.setColor(Color.black);
                    drawString(g, x, y, defaultFont, "Client here");
                    break;
                }
                g.setColor(Color.black);
                drawString(g, x, y, defaultFont, "Green");
                break;
        }

        repaint();

    }

    @Override
    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        if (id == 0) {
            Location lRobot = tmodel.getAgPos(0);
            if (lRobot.equals(tmodel.getAgPos(1))) {
                c = Color.BLACK;
                super.drawAgent(g, x, y, c, -1);
                g.setColor(Color.white);
                super.drawString(g, x, y, defaultFont, "All Taxis");
            } else if (!lRobot.equals(tmodel.lRed) && !lRobot.equals(tmodel.lBlue) && !lRobot.equals(tmodel.lYellow)
                    && !lRobot.equals(tmodel.lGreen)) {
                c = Color.CYAN;
                if (!tmodel.agents[0].isAvailable())
                    c = Color.orange;
                super.drawAgent(g, x, y, c, -1);
                g.setColor(Color.black);
                super.drawString(g, x, y, defaultFont, "Taxi1");
            }
        }
        if (id == 1) {
            Location lRobot = tmodel.getAgPos(1);
            if (lRobot.equals(tmodel.getAgPos(0))) {
                c = Color.BLACK;
                super.drawAgent(g, x, y, c, -1);
                g.setColor(Color.white);
                super.drawString(g, x, y, defaultFont, "All Taxis");
            } else if (!lRobot.equals(tmodel.lRed) && !lRobot.equals(tmodel.lBlue) && !lRobot.equals(tmodel.lYellow)
                    && !lRobot.equals(tmodel.lGreen)) {
                c = Color.MAGENTA;
                if (!tmodel.agents[1].isAvailable())
                    c = Color.orange;
                super.drawAgent(g, x, y, c, -1);
                g.setColor(Color.black);
                super.drawString(g, x, y, defaultFont, "Taxi2");
            }
        }
    }

}
