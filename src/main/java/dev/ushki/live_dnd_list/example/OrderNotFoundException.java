package dev.ushki.live_dnd_list.example;

public class OrderNotFoundException extends RuntimeException {
    OrderNotFoundException(Long id) {
        super("Could not find order " + id);
    }
}
