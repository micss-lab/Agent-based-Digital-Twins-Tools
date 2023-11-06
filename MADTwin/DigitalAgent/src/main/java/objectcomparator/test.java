package objectcomparator;

import helpers.Delay;

import java.util.PriorityQueue;
import java.util.Queue;

public class test {

    public static void main(String[] args) {
        Item c1 = new Item(1, 100.0, "item1");
        Item c2 = new Item(3, 50.0, "item3");
        Item c3 = new Item(2, 300.0, "item2");
        Queue<Item> itemQueue = new PriorityQueue<>(new ItemComparator());
        itemQueue.add(c1);
        itemQueue.add(c2);
        itemQueue.add(c3);
        while (!itemQueue.isEmpty()) {
            System.out.println(itemQueue.poll());
            Delay.msSleep(5000);
        }
    }
}
