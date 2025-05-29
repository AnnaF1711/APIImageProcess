package org.example;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.awt.Point;
import java.util.List;

public class Labyrinth extends JPanel {
    public static final int SIZE=30; //גודל המבוך
    private BufferedImage image;
    private List<Point> solutionPath = new ArrayList<>();


    public Labyrinth() {
        this.setBounds(0,0,MainFrame.WIDTH,MainFrame.HEIGHT);
        int expectedPoints = SIZE*SIZE;
        this.image = new BufferedImage(SIZE,SIZE,BufferedImage.TYPE_INT_RGB);//למלא את הפיקסלים
        paintPixels(this.image, expectedPoints);
        this.setLayout(null);
        JButton button = new JButton("Check solution");
        button.setBounds(MainFrame.WIDTH/2-100,MainFrame.HEIGHT-86,200,43);
        button.setFont(new Font("Monospaced", Font.PLAIN, 18));
        this.add(button);
        button.addActionListener(e -> {
            JLabel label = new JLabel("", SwingConstants.CENTER);
            label.setFont(new Font("Monospaced", Font.PLAIN, 30));
            label.setBounds(0,MainFrame.HEIGHT/2-MainFrame.HEIGHT/10,MainFrame.WIDTH,MainFrame.HEIGHT/10);
            label.setForeground(Color.WHITE);
            label.setOpaque(true);
            label.setBackground(Color.RED);
            if(!checkSolution()){
                label.setText("Solution doesn't exist!");
                this.add(label);
            }

        });

    }

    public boolean checkSolution() {
        this.solutionPath.clear();//מנקה את המסלול הקודם לפני בדיקה חדשה

        if (this.image.getRGB(0, 0) == Color.BLACK.getRGB() ||//אם הפיקסל ההתחלתי או הסופי שחור, אין פיתרון
                this.image.getRGB(SIZE - 1, SIZE - 1) == Color.BLACK.getRGB()) {
            return false;
        }

        boolean[][] visited = new boolean[SIZE][SIZE];//ברירת מחדל false
        boolean found = dfs(0, 0, visited);//תחילת חיפוש

        repaint(); // כדי לעדכן את הציור עם המסלול
        return found;
    }
    private boolean isValid(int x, int y, boolean[][] visited) {
        return x >= 0 && x < SIZE &&
                y >= 0 && y < SIZE &&
                this.image.getRGB(x, y) != Color.BLACK.getRGB() &&
                !visited[x][y];
    }

    private boolean dfs(int x, int y, boolean[][] visited) {//בודקת עבור כל פיקסל אם אפשר להתקדם
        if (x == SIZE - 1 && y == SIZE - 1) {//תנאי עצירה
            this.solutionPath.add(new Point(x, y)); // הוספת נקודת הסיום למסלול
            return true;
        }

        visited[x][y] = true;//סימון פיקסל כבודק
        this.solutionPath.add(new Point(x, y)); // הוספת הנקודה הנוכחית למסלול

        int[][] directions = {
                {1, 0},   // ימינה
                {0, 1},   // למטה
                {-1,0},   //שמאלה
                {0, -1}   // למעלה
        };

        for (int[] dir : directions) {//בדיקה עבור על כיוון אם אפשר להתקדם
            int newX = x + dir[0];//מיקום אינדקסים
            int newY = y + dir[1];

            if (isValid(newX, newY, visited)) {
                if (dfs(newX, newY, visited)) {//בדיקת הפיקסל הבא
                    return true;
                }
            }
        }

        this.solutionPath.remove(this.solutionPath.size() - 1); // אם לא נמצא פתרון מהנקודה הזו נחזור אחורה
        return false;
    }


    public void paintPixels(BufferedImage image, int expectedPoints) {
       //צביעת הפיקסלים השחורים
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                image.setRGB(x, y, Color.BLACK.getRGB());
            }
        }
       //צביעת הפיקסלים הלבנים
        try {
            HttpResponse<String> response = Unirest.get("https://app.seker.live/fm1/get-points")
                    .queryString("width",SIZE).queryString("height",SIZE).asString();//המבוך בגודל שקבענו
            JSONArray pointsArray = new JSONArray(response.getBody());

            Set<String> pointsSeen = new HashSet<>();//הנקודות שעברנו עליהן
            for (int i = 0; i < pointsArray.length(); i++) {
                JSONObject json = pointsArray.getJSONObject(i);

                int x = (int) json.getDouble("x");
                int y = (int) json.getDouble("y");
                String key = x + "," + y;

                if (!pointsSeen.contains(key)) {
                pointsSeen.add(key);
                if (x >= 0 && x < SIZE && y >= 0 && y < SIZE) {//בדיקה שלא יצאנו מהגבולות
                    image.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }

        repaint();
    }
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.image != null) {
            int imageWidth = getWidth();
            int imageHeight = getHeight() - 50;//מקום לכפתור

            g.drawImage(this.image, 0, 0, imageWidth, imageHeight, null);

            // צביעת המסלול בירוק
            g.setColor(Color.GREEN);
            double pixelWidth = (double) imageWidth / SIZE;
            double pixelHeight = (double) imageHeight / SIZE;

            for (Point p : solutionPath) {
                //ציור ריבוע קטן יותר בתוך הפיקסל כדי להשאיר שוליים וכדי לא לחרוג
                if (this.image.getRGB(p.x, p.y) == Color.WHITE.getRGB()) {

                    int x = (int) (p.x * pixelWidth + pixelWidth * 0.2);
                    int y = (int) (p.y * pixelHeight + pixelHeight * 0.2);
                    int w = (int) (pixelWidth * 0.6);
                    int h = (int) (pixelHeight * 0.6);

                    g.fillRect(x, y, w, h);
                }
            }
        }
    }
}
