package com.example.edgers_lottery;

import static org.junit.Assert.*;

import com.example.edgers_lottery.models.User;
import com.example.edgers_lottery.services.LotteryService;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;

public class LotteryServiceTest {

    private ArrayList<User> createMockWaitlist(int size) {
        ArrayList<User> waitList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            User user = new User();
            user.setId("user_" + i);
            user.setName("User " + i);
            waitList.add(user);
        }
        return waitList;
    }

    @Test
    public void testLotteryChoosesCorrectAmount() {
        ArrayList<User> waitList = createMockWaitlist(10);
        int remainingCapacity = 3;

        Map.Entry<ArrayList<User>, ArrayList<User>> result = LotteryService.runLottery(waitList, remainingCapacity);

        assertEquals(3, result.getKey().size());   // chosen list should have 3
        assertEquals(7, result.getValue().size()); // not invited should have 7
    }

    @Test
    public void testLotteryWhenCapacityExceedsWaitlist() {
        ArrayList<User> waitList = createMockWaitlist(5);
        int remainingCapacity = 10; // more capacity than people

        Map.Entry<ArrayList<User>, ArrayList<User>> result = LotteryService.runLottery(waitList, remainingCapacity);

        assertEquals(5, result.getKey().size());   // everyone gets invited
        assertEquals(0, result.getValue().size()); // no one left out
    }

    @Test
    public void testLotteryWhenCapacityEqualsWaitlist() {
        ArrayList<User> waitList = createMockWaitlist(5);
        int remainingCapacity = 5;

        Map.Entry<ArrayList<User>, ArrayList<User>> result = LotteryService.runLottery(waitList, remainingCapacity);

        assertEquals(5, result.getKey().size());
        assertEquals(0, result.getValue().size());
    }

    @Test
    public void testLotteryTotalCountIsPreserved() {
        ArrayList<User> waitList = createMockWaitlist(10);
        int remainingCapacity = 4;

        Map.Entry<ArrayList<User>, ArrayList<User>> result = LotteryService.runLottery(waitList, remainingCapacity);

        // chosen + not invited should always equal original waitlist size
        assertEquals(10, result.getKey().size() + result.getValue().size());
    }

    @Test
    public void testLotteryIsRandom() {
        // run lottery twice and check results differ at least sometimes
        ArrayList<User> waitList1 = createMockWaitlist(20);
        ArrayList<User> waitList2 = new ArrayList<>(waitList1);

        Map.Entry<ArrayList<User>, ArrayList<User>> result1 = LotteryService.runLottery(waitList1, 5);
        Map.Entry<ArrayList<User>, ArrayList<User>> result2 = LotteryService.runLottery(waitList2, 5);

        // extract chosen IDs
        ArrayList<String> ids1 = new ArrayList<>();
        ArrayList<String> ids2 = new ArrayList<>();
        for (User u : result1.getKey()) ids1.add(u.getId());
        for (User u : result2.getKey()) ids2.add(u.getId());

        // with 20 users and only 5 chosen, results will almost never be identical
        assertFalse("Lottery should produce different results each time", ids1.equals(ids2));
    }
}