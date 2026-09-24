package com.example.demo.cart.entity;
import com.example.demo.cartitem.CartItem;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import com.example.demo.security.user.entity.User;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cart")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    


    @OneToOne
    @JoinColumn(name="user_id")
    private  User user;

    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER)
     @JsonBackReference       
    private List<CartItem> cartItems =
            new ArrayList<>();

    // getters setters

    public Long getId() {
        return id;
    }

   

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }
    public User getUser() {
    return user;
}

public void setUser(User user) {
    this.user = user;
}
public void removeCartItem(CartItem cartItem) {

        cartItems.remove(cartItem);

        cartItem.setCart(null);
    }
 
}