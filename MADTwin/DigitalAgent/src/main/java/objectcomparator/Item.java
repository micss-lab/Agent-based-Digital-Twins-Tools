package objectcomparator;

public class Item implements Comparable<Item> {
    private int itemId;
    private double itemWeight;
    private String itemName;

    public Item(int itemId, double itemWeight, String itemName) {
        this.itemId = itemId;
        this.itemWeight = itemWeight;
        this.itemName = itemName;
    }

    @Override
    public int compareTo(Item o) {
        return o.itemId > this.itemId ? 1 : -1;
    }

    public double getItemWeight() {
        return itemWeight;
    }

    public int getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    @Override
    public String toString() {
        return "Item{" +
                "itemId=" + itemId +
                ", itemWeight=" + itemWeight +
                ", itemName='" + itemName + '\'' +
                '}';
    }
}
