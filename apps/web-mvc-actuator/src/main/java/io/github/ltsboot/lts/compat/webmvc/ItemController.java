package io.github.ltsboot.lts.compat.webmvc;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final Map<Long, Item> items = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @PostMapping
    public ResponseEntity<Item> create(@Valid @RequestBody ItemRequest request) {
        Item item = new Item(sequence.incrementAndGet(), request.getName(), request.getQuantity());
        items.put(item.getId(), item);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(item.getId())
                .toUri();
        return ResponseEntity.created(location).body(item);
    }

    @GetMapping("/{id}")
    public Item get(@PathVariable("id") long id) {
        Item item = items.get(id);
        if (item == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item " + id + " not found");
        }
        return item;
    }
}
