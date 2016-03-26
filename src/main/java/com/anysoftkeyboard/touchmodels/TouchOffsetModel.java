package com.anysoftkeyboard.touchmodels;

public interface TouchOffsetModel {

    /**
     * Predicts the x-coordinate offset from the touch point.
     * @param x The y-coordinate of a touch point
     * @param y The y-coordinate of a touch point
     * @return Predicted x-coordinate offset
     */
    int getOffsetX(int x, int y);

    /**
     * Predicts the y-coordinate offset from the touch point.
     * @param x The y-coordinate of a touch point
     * @param y The y-coordinate of a touch point
     * @return Predicted y-coordinate offset
     */
    int getOffsetY(int x, int y);
}