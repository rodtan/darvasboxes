package boxes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author rtan
 */
public class DarvasBox {

    public static void main(String[] args) {
        int time_range = 90; // days
        int MAX_IMAGE_HEIGHT = 180;
        int MAX_IMAGE_WIDTH = 420;
        int DARVAS = 2;
        int DEBUG = 0;

        String debug_file = "c:\\rod\\darvas.debug";
        int FIND_BOX_TOP_AGAIN = -100;
        // Set default stock:
        String stock = "C";


        // Get today's date (end date):
        Calendar eDate = Calendar.getInstance();
        int ey = eDate.get(Calendar.YEAR);
        int yem = eDate.get(Calendar.MONTH); // already 0 based
        int ed = eDate.get(Calendar.DAY_OF_MONTH);

        // Set Start Date
        Calendar sDate = Calendar.getInstance();
        sDate.add(Calendar.DAY_OF_MONTH, -time_range);
        int sy = sDate.get(Calendar.YEAR);
        int ysm = sDate.get(Calendar.MONTH);
        int sd = sDate.get(Calendar.DAY_OF_MONTH);

        // Set the historical quote here:
        String hist_quote = "http://table.finance.yahoo.com/table.csv?s=" + stock +
                "&a=" + ysm + "&b=" + sd + "&c=" + sy + "&d=" + yem + "&e=" + ed + "&f=" + ey +
                "&g=d&q=q&y=0&x=.csv";
        System.out.println(hist_quote);

        // Download the csv file into an array:
        try {
            URL yahoo = new URL(hist_quote.toString());
            URLConnection yc = yahoo.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            String inputLine;
            String[] aFoo = new String[9000];

            // Get number of points we'll be plotting:
            int num_pts = 0;
            while ((inputLine = in.readLine()) != null) {
                if (num_pts == 0) { // skip first line
                    num_pts++;
                } else {
                    aFoo[num_pts++] = inputLine;
                //System.out.println(quotes[quoteCounter-1]);

                }
            }
            num_pts--; // cater for end of loop increment

            // Reverse the entries
            String[] aQuotes = new String[num_pts];
            for (int i = num_pts; i > 0; i--) {
                aQuotes[Math.abs(num_pts - i)] = aFoo[i];
            }
            //System.out.println(aQuotes[aQuotes.length-1]);
            
            in.close();

            // Populate the high, low, and volume arrays:
            int[] aVolume = new int[num_pts];
            float[] aClose = new float[num_pts];
            float[] aLow = new float[num_pts];
            float[] aHigh = new float[num_pts];
            float[] aOpen = new float[num_pts];
            float[] aAdjClose = new float[num_pts];
            String[] aDate = new String[num_pts];
            for (int t = 0; t < aQuotes.length; t++) {
                String[] tokens = aQuotes[t].split(",");
                aDate[t] = tokens[0].substring(0, tokens[0].length() - 4);
                aOpen[t] = Float.parseFloat(tokens[1]);
                aHigh[t] = Float.parseFloat(tokens[2]);
                aLow[t] = Float.parseFloat(tokens[3]);
                aClose[t] = Float.parseFloat(tokens[4]);
                aVolume[t] = Integer.parseInt(tokens[5]);
                aAdjClose[t] = Float.parseFloat(tokens[6]);
            }

            // get maximum values
            int max_volume = maxInt(aVolume);
            int min_volume = minInt(aVolume);
            float max_high = maxFloat(aHigh);
            float min_low = minFloat(aLow);
            float y_range = max_high - min_low;

            // setup image dimensions
            int width = time_range * 7;
            int height = time_range * 5;
            if (height < MAX_IMAGE_HEIGHT) {
                height = MAX_IMAGE_HEIGHT;
            }
            if (width < MAX_IMAGE_WIDTH) {
                width = MAX_IMAGE_WIDTH;
            }

            int edge_padding = 3;

            // Create the image
            BufferedImage bimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            // Colours
            // use
//            Color.WHITE;
//            Color.MAGENTA; // for navy
//            Color.GRAY;
//            Color.RED;
//            Color.BLUE;
//            Color.YELLOW;
//            Color.BLACK;
//

            //Layout
            float maxval = max_high;
            int num_entries = aQuotes.length;
            int nval = num_entries;

            int vmargin = 20;
            int hmargin = 38;
            float col_size = (float)Math.floor((width - hmargin) / num_entries);
            float base = col_size;

            float x_size_plot = num_entries * base;
            float y_size_plot = height - 2 * vmargin;
            float xsize = x_size_plot;
            float ysize = y_size_plot;

            // Title
            int titlefont = 3;
            String title = stock + " (" + sy + "-" + ysm + 1 + "-" + sd + " to " +
                    ey + "-" + yem + 1 + "-" + ed;
            int text_size = titlefont * title.length();
            int txtsz = text_size;
            
            // Center the title
            int title_x_pos = (int) (hmargin + (x_size_plot - text_size) / 2);
            if (title_x_pos < 1) {
                title_x_pos = 1;
            }
            int title_y_pos = edge_padding;
            int xpos = title_x_pos;
            int ypos = title_y_pos;

            //Print Title
            Graphics2D g2 = (Graphics2D) bimage.getGraphics();
            FontRenderContext frc = g2.getFontRenderContext();
            Font f = new Font("Helvetica", Font.PLAIN, 3);
            TextLayout tl = new TextLayout(title, f, frc);
            g2.setColor(Color.BLACK);
            tl.draw(g2, title_x_pos, title_y_pos);

// ===================================================================
// Draw grid lines at the Y tick heights and prints the tick labels at the same time
// ===================================================================

// y labels and grid lines
            int labelfont = 2;
            int num_grid_lines = 10;
            int ngrid = num_grid_lines;

            // Setup scaling factors:
            float data_units_per_line=max_high/num_grid_lines;
            float pixels_per_line = y_size_plot / (num_grid_lines + 1);
            float dydat = data_units_per_line;
            float dypix = pixels_per_line;

            //Iterate over the y ticks
            for (int i=0;i<=(num_grid_lines+1);i++) {
                float y_pos_data = i * data_units_per_line;
                float y_pos_pixels = vmargin + y_size_plot - (int)(i*pixels_per_line);
                txtsz = (int)(labelfont*y_pos_data);
                int txtht = (int)(labelfont);

                int xpos_label=(int)((hmargin-txtsz)/2);
                if (xpos_label < 1) xpos_label = 1;
                tl = new TextLayout("test",f,frc);
                tl.draw(g2, xpos_label, dypix);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static int maxInt(int[] arr) {
        int result = arr[0];
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] > result) {
                result = arr[i];
            }
        }
        return result;
    }

    public static int minInt(int[] arr) {
        int result = arr[0];
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] < result) {
                result = arr[i];
            }
        }
        return result;
    }

    public static float maxFloat(float[] arr) {
        float result = arr[0];
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] > result) {
                result = arr[i];
            }
        }
        return result;
    }

    public static float minFloat(float[] arr) {
        float result = arr[0];
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] < result) {
                result = arr[i];
            }
        }
        return result;
    }
}
