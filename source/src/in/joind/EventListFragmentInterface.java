package in.joind;

public interface EventListFragmentInterface {
    void performEventListUpdate();

    void setEventSortOrder(int sortOrder);

    int getEventSortOrder();

    void filterByString(CharSequence s);
}
