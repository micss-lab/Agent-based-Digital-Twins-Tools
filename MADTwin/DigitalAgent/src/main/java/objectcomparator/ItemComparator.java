package objectcomparator;

import java.util.Comparator;

class ItemComparator implements Comparator<Item> {
    @Override
    public int compare(Item item1, Item item2)
    {
        return item1.getItemWeight() < item2.getItemWeight() ? 1 : -1;
    }
}