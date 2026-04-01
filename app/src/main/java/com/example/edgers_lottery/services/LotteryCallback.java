package com.example.edgers_lottery.services;

/**
 * Callback interface for receiving the result of a lottery operation.
 */
public interface LotteryCallback {

    /**
     * Called when the lottery operation has completed.
     * @param message a message describing the result
     */
    void onComplete(String message);
}