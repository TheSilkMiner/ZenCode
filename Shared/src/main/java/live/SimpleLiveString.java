package live;

import java.util.function.BiConsumer;
import listeners.ListenerHandle;
import listeners.ListenerList;

public final class SimpleLiveString implements MutableLiveString {
    private final ListenerList<BiConsumer<String, String>> listeners = new ListenerList<BiConsumer<String, String>>();
    private String value;
    
    public SimpleLiveString(String value) {
        this.value = value;
    }
    
    @Override
    public ListenerHandle<BiConsumer<String, String>> addListener(BiConsumer<String, String> listener) {
        return listeners.add(listener);
    }
    
    @Override
    public void setValue(String value) {
        if (value.equals(this.value))
            return;
        String oldValue = this.value;
        this.value = value;
        listeners.accept(listener -> 
        listener.accept(oldValue, this.value));
    }
    
    public String getValue() {
        return value;
    }
}
