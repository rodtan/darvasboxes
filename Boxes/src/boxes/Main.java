/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package boxes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import java.io.BufferedReader;
import java.net.URL;
import java.net.URLConnection;

import java.util.Collections;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.annotations.XYBoxAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.chart.axis.Timeline;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.Range;
import org.jfree.data.time.DateRange;
import org.jfree.data.time.Day;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.ui.RectangleEdge;

/**
 *
 * @author rtan
 */
public class Main {

    private static final String YAHOO_URL = "http://table.finance.yahoo.com/table.csv?";
    private static final String YAHOO_START_MONTH = "&a=";
    private static final String YAHOO_START_DAY = "&b=";
    private static final String YAHOO_START_YEAR = "&c=";
    private static final String YAHOO_END_MONTH = "&d=";
    private static final String YAHOO_END_DAY = "&e=";
    private static final String YAHOO_END_YEAR = "&f=";
    private static final String YAHOO_RESOLUTION = "&g=d";
    private static final String YAHOO_TICKER_SYMBOL = "s=";
    private static final String YAHOO_CONSTANTS = "&q=q&y=0&x=.csv";
    private static final int YAHOO_RANGE = 90; // in days
    private static final String PNG_FILEPATH = "c:\\rod\\darvas_";
    private static final String PNG_FILE_EXT = ".png";
    private static final String TEST_PNG_FILEPATH = "c:\\rod\\test_";

    public Main() {
    }

    public String getQuoteString(String sym) {
        StringBuffer quote = new StringBuffer();
        Calendar today = Calendar.getInstance();
        Calendar start = Calendar.getInstance();
        start.add(Calendar.DAY_OF_MONTH, -YAHOO_RANGE);
        quote.append(Main.YAHOO_URL);
        quote.append(Main.YAHOO_TICKER_SYMBOL);
        quote.append(sym);

        quote.append(Main.YAHOO_START_MONTH);
        quote.append(start.get(Calendar.MONTH));
        quote.append(Main.YAHOO_START_DAY);
        quote.append(start.get(Calendar.DAY_OF_MONTH));
        quote.append(Main.YAHOO_START_YEAR);
        quote.append(start.get(Calendar.YEAR));

        quote.append(Main.YAHOO_END_MONTH);
        quote.append(today.get(Calendar.MONTH));
        quote.append(Main.YAHOO_END_DAY);
        quote.append(today.get(Calendar.DAY_OF_MONTH));
        quote.append(Main.YAHOO_END_YEAR);
        quote.append(today.get(Calendar.YEAR));

        quote.append(Main.YAHOO_RESOLUTION);
        quote.append(Main.YAHOO_CONSTANTS);
        return quote.toString();

    }

    public List getQuotes(String sym) {
        List quotes = null;
        try {
            URL yahoo = new URL(getQuoteString(sym));
            URLConnection yc = yahoo.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            String inputLine;
            quotes = new ArrayList();
            while ((inputLine = in.readLine()) != null) {
                //System.out.println(inputLine);
                if ((parse(inputLine)) != null) {
                    quotes.add(parse(inputLine));
                }
            }
            in.close();
            Collections.reverse(quotes);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return quotes;
    }

    public void process(String sym) {
        /** get the symbol from tables.finance.yahoo.com
        http://table.finance.yahoo.com/table.csv?
         * s=STOCKNAME&
         * a=MON&
         * b=DAY&
         * c=YEAR&
         * d=MON&
         * e=DAY&
         * f=YEAR&
         * g=d&
         * q=q&
         * y=0&
         * z=STOCKNAME&x=.csv
         **/
        //System.out.println(quote.toString());
        try {
            List quotes = getQuotes(sym);
            if (quotes != null) {
                int state = 1; // initial state
                double boxTop = 0.0;
                double boxBottom = 1000000.0;

                //Date[] sBox = new Date[10];
                int[] boxStarts = new int[10];
                int[] boxEnds = new int[10];
                for (int j = 0; j < 10; j++) {
                    boxStarts[j] = -1;
                    boxEnds[j] = -1;
                }
                String[] buySell = new String[10];
                int lastBox = 0;

                int boxCounter = 0;
                for (int i = 0; i < quotes.size(); i++) {
                    DailyQuote dQuote = (DailyQuote) quotes.get(i);
                    switch (state) {
                        case 1:
                            if (boxStarts[boxCounter] == -1) {
                                boxStarts[boxCounter] = i;
                                boxEnds[boxCounter] = i;
                                lastBox = -1;
                            }
                            if (boxTop >= dQuote.getHighPrice()) {
                                state = 2;
                            } else {
                                state = 1;
                                boxTop = dQuote.getHighPrice();
                            }
                            break;
                        case 2:
                            if (boxTop >= dQuote.getHighPrice()) {
                                state = 3;
                            } else {
                                state = 1;
                                boxTop = dQuote.getHighPrice();
                            }
                            break;
                        case 3:
                            if (boxTop >= dQuote.getHighPrice()) {
                                if (boxBottom <= dQuote.getLowPrice()) {
                                    state = 4;
                                } else {
                                    state = 3;
                                    boxBottom = dQuote.getLowPrice();
                                }
                            } else {
                                state = 1;
                                boxTop = dQuote.getHighPrice();
                            }
                            break;
                        case 4:
                            if (boxTop >= dQuote.getHighPrice()) {
                                if (boxBottom <= dQuote.getLowPrice()) {
                                    state = 5;
                                    lastBox = i;
                                } else {
                                    state = 3;
                                    boxBottom = dQuote.getLowPrice();
                                }
                            } else {
                                state = 1;
                                boxTop = dQuote.getHighPrice();
                            }
                            break;
                        case 5:
                            if (dQuote.getHighPrice() > boxTop) {
                                // need previous day's date
                                buySell[boxCounter] = "B";
                                boxEnds[boxCounter++] = i - 1;
                                boxStarts[boxCounter] = i;
                                state = 1;
                                lastBox = -1;
                            } else if (dQuote.getLowPrice() < boxBottom) {
                                // need previous day's date
                                buySell[boxCounter] = "S";
                                boxEnds[boxCounter++] = i - 1;
                                boxStarts[boxCounter] = i;
                                state = 1;
                                lastBox = -1;
                            } else {
                                state = 5;
                            }
                            break;
                        default:
                            System.out.println("ERROR!");
                            break;
                    } // end switch
                } // end for
                if (lastBox > -1) {
                    buySell[boxCounter] = buySell[boxCounter - 1];
                    boxEnds[boxCounter++] = lastBox;
                }
                createChart(sym, quotes, boxStarts, boxEnds, buySell, boxCounter - 1, lastBox);
            } // if quotes != null
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void createChart(String sym, List quotes, int[] boxStarts, int[] boxEnds, String[] buySell, int boxCounter, int lastBox) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        JFreeChart chart = getOHLCChart(quotes, sym);
        Color sellColor = new Color(255, 0, 0, 60);
        Color buyColor = new Color(0, 0, 255, 60);
        Color startColor = new Color(255, 255, 255, 60);

        try {
            for (int j = 0; j <= boxCounter; j++) {
                DailyQuote b1 = (DailyQuote) quotes.get(boxStarts[j]);
                DailyQuote b2 = (DailyQuote) quotes.get(boxEnds[j]);
                if (j > 0) {
                    System.out.println("Box " + j + ": " + sdf.format(b1.getTradeDate()) +
                            ", ending " + sdf.format(b2.getTradeDate()) + " : " + buySell[j-1]);
                } else {
                    System.out.println("Box " + j + ": " + sdf.format(b1.getTradeDate()) +
                            ", ending " + sdf.format(b2.getTradeDate()));
                }
                double high = getHighestHigh(quotes, boxStarts[j], boxEnds[j]) + 0.1;
                double low = getLowestLow(quotes, boxStarts[j], boxEnds[j]) - 0.1;
                Color color = startColor;
                if (j > 0) {
                    color = (buySell[j - 1].equals("S")) ? sellColor : buyColor;
                }
                XYBoxAnnotation xyba = new XYBoxAnnotation(new Day(b1.getTradeDate()).getFirstMillisecond(), low,
                        new Day(b2.getTradeDate()).getLastMillisecond(), high, new BasicStroke(0.0F), Color.black, color);
                CandlestickRenderer renderer = (CandlestickRenderer) chart.getXYPlot().getRenderer();
                renderer.addAnnotation(xyba);
            }

            if (lastBox > -1) {
                DailyQuote b3 = (DailyQuote) quotes.get(lastBox);
                System.out.println("Last box fully formed: " + sdf.format(b3.getTradeDate()));

            } else {
                System.out.println("No box formed yet");
            }
            saveChart(chart, getPNGFileName(sym));
//            String newImagePath = TEST_PNG_FILEPATH + sym + PNG_FILE_EXT;
//            saveImage(img, newImagePath);

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("ERROR Printing png");
        }
    }

    public void saveChart(JFreeChart chart, String filepath) throws Exception {
        FileOutputStream png = new FileOutputStream(filepath);
        ChartUtilities.writeChartAsPNG(png, chart, 600, 400);
    }

    public String getPNGFileName(String sym) {
        return PNG_FILEPATH + sym + PNG_FILE_EXT;
    }

    public JFreeChart getOHLCChart(List quotes, String sym) {
        OHLCDataset dataset = getOHLCDataSet(quotes, sym);
        JFreeChart chart = ChartFactory.createCandlestickChart(
                sym, "Date", "Price", dataset, false);
        chart.removeLegend();
        XYPlot xyplot = chart.getXYPlot();
        DateAxis axis = (DateAxis) xyplot.getDomainAxis();
        chart.getXYPlot().getRangeAxis().setRange(
                new Range(getLowestLow(quotes) - .5,
                getHighestHigh(quotes) + .5));
        axis.setTimeline(SegmentedTimeline.newMondayThroughFridayTimeline());
        axis.setDateFormatOverride(new SimpleDateFormat("MM/dd"));
        axis.setVerticalTickLabels(true);
        chart.getXYPlot().setDomainAxis(axis);
        return chart;
    }

    private DailyQuote parse(String csvLine) {
        if (!csvLine.startsWith("Date")) {
            DailyQuote quote = new DailyQuote();
            try {
                String[] tokens = csvLine.split(",");
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                //System.out.println(csvLine);
                quote.setTradeDate(df.parse(tokens[0]));
                quote.setOpenPrice(Float.parseFloat(tokens[1]));
                quote.setHighPrice(Float.parseFloat(tokens[2]));
                quote.setLowPrice(Float.parseFloat(tokens[3]));
                quote.setClosePrice(Float.parseFloat(tokens[4]));
                quote.setVolume(Integer.parseInt(tokens[5]));
                quote.setAdjClose(Float.parseFloat(tokens[6]));

            } catch (Exception ex) {
                //System.out.println(csvLine);
                ex.printStackTrace();
            }
            return quote;
        } else {
            return null;
        }

    }

    public double getLowestLow(List quotes) {
        return getLowestLow(quotes, 0, quotes.size() - 1);
    }

    public double getLowestLow(List quotes, int start, int end) {
        double result = -1.0;
        for (int i = start; i <= end; i++) {
            DailyQuote dQuote = (DailyQuote) quotes.get(i);
            if (dQuote.getLowPrice() < result) {
                result = dQuote.getLowPrice();
            } else if (result == -1.0) {
                result = dQuote.getLowPrice();
            }
        }
        return result;
    }

    public double getHighestHigh(List quotes) {
        return getHighestHigh(quotes, 0, quotes.size() - 1);
    }

    public double getHighestHigh(List quotes, int start, int end) {
        double result = 0.0;
        for (int i = start; i <= end; i++) {
            DailyQuote dQuote = (DailyQuote) quotes.get(i);
            if (dQuote.getHighPrice() > result) {
                result = dQuote.getHighPrice();
            } else if (result == 0.0) {
                result = dQuote.getHighPrice();
            }
        }
        return result;
    }

    public OHLCDataset getOHLCDataSet(List quotes, String sym) {
        Date[] date = new Date[quotes.size()];
        double[] high = new double[quotes.size()];
        double[] low = new double[quotes.size()];
        double[] open = new double[quotes.size()];
        double[] close = new double[quotes.size()];
        double[] vol = new double[quotes.size()];
        for (int i = 0; i < quotes.size(); i++) {
            //System.out.println(quotes.get(i) + " " + i);
            DailyQuote dQuote = (DailyQuote) quotes.get(i);
            date[i] = dQuote.getTradeDate();
            high[i] = dQuote.getHighPrice();
            low[i] = dQuote.getLowPrice();
            open[i] = dQuote.getOpenPrice();
            close[i] = dQuote.getAdjClose();
            vol[i] = dQuote.getVolume();
        }
        OHLCDataset dataset = new DefaultHighLowDataset(
                sym, date, high, low, open, close, vol);
        return dataset;
    }

    public static void main(String[] args) {
        // TODO code application logic here
        Main newMain = new Main();
        String[] symbols = {"C", "ATVI"};
        for (int i = 0; i < symbols.length; i++) {
            System.out.println("Doing it for " + symbols[i]);
            newMain.process(symbols[i]);
            System.out.println();
        }

    }
}
