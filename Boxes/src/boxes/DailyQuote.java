/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package boxes;

import java.util.Date;

/**
 *
 * @author rtan
 */
public class DailyQuote {
    private Date tradeDate;
    private double openPrice;
    private double highPrice;
    private double lowPrice;
    private double closePrice;
    private int volume;
    private double adjClose;
    
    public DailyQuote() {
        tradeDate = new Date();
    }

    /**
     * @return the tradeDate
     */
    public Date getTradeDate() {
        return tradeDate;
    }

    /**
     * @param tradeDate the tradeDate to set
     */
    public void setTradeDate(Date tradeDate) {
        this.tradeDate = tradeDate;
    }

    /**
     * @return the openPrice
     */
    public double getOpenPrice() {
        return openPrice;
    }

    /**
     * @param openPrice the openPrice to set
     */
    public void setOpenPrice(double openPrice) {
        this.openPrice = openPrice;
    }

    /**
     * @return the highPrice
     */
    public double getHighPrice() {
        return highPrice;
    }

    /**
     * @param highPrice the highPrice to set
     */
    public void setHighPrice(double highPrice) {
        this.highPrice = highPrice;
    }

    /**
     * @return the lowPrice
     */
    public double getLowPrice() {
        return lowPrice;
    }

    /**
     * @param lowPrice the lowPrice to set
     */
    public void setLowPrice(double lowPrice) {
        this.lowPrice = lowPrice;
    }

    /**
     * @return the closePrice
     */
    public double getClosePrice() {
        return closePrice;
    }

    /**
     * @param closePrice the closePrice to set
     */
    public void setClosePrice(double closePrice) {
        this.closePrice = closePrice;
    }

    /**
     * @return the volume
     */
    public int getVolume() {
        return volume;
    }

    /**
     * @param volume the volume to set
     */
    public void setVolume(int volume) {
        this.volume = volume;
    }

    /**
     * @return the adjClose
     */
    public double getAdjClose() {
        return adjClose;
    }

    /**
     * @param adjClose the adjClose to set
     */
    public void setAdjClose(double adjClose) {
        this.adjClose = adjClose;
    }

}
