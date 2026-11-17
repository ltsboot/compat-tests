package io.github.ltsboot.lts.compat.webmvc;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

public class ItemRequest {

    @NotBlank
    private String name;

    @Min(1)
    private int quantity;

    public ItemRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
