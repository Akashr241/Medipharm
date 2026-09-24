import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import {
    myCart,
    removeFromCart
} from "../services/cartService";


const Cart = () => {

    const navigate = useNavigate();


    // ==========================================
    // STATE
    // ==========================================

    const [cart, setCart] = useState(null);

    const [loading, setLoading] = useState(true);

    const [removingItemId, setRemovingItemId] =
        useState(null);


    // ==========================================
    // FETCH LATEST CART FROM BACKEND
    // ==========================================

    const fetchCart = async () => {

        try {

            console.log(
                "========== FETCHING LATEST CART =========="
            );

            const response = await myCart();

            console.log(
                "Latest Cart Response:",
                response
            );

            setCart(response);

        } catch (error) {

            console.error(
                "Failed to load cart:",
                error
            );

            setCart(null);

        }

    };


    // ==========================================
    // LOAD CART WHEN PAGE OPENS
    // ==========================================

    useEffect(() => {

        const loadCart = async () => {

            setLoading(true);

            await fetchCart();

            setLoading(false);

        };

        loadCart();

    }, []);


    // ==========================================
    // REMOVE CART ITEM
    // ==========================================
const handleRemoveFromCart = async (cartItemId) => {

    try {

        console.log(
            "========== REMOVE CART ITEM =========="
        );

        console.log(
            "Cart Item ID:",
            cartItemId
        );

        setRemovingItemId(cartItemId);

        // ==================================
        // DELETE FROM BACKEND
        // ==================================

        await removeFromCart(cartItemId);

        console.log(
            "Cart item deleted successfully from backend"
        );


        // ==================================
        // UPDATE FRONTEND CART STATE
        // REMOVE ONLY THE DELETED ITEM
        // ==================================

            console.log(
                "Fetching updated cart after deletion..."
            );

            const updatedCart =
                await myCart();


            console.log(
                "Updated Cart:",
                updatedCart
            );


            // Update React state

            setCart(updatedCart);


            console.log(
                "Cart UI updated successfully"
            );





        console.log(
            "Cart UI updated successfully"
        );

    } catch (error) {

        console.error(
            "Failed to remove product:",
            error
        );

        alert(
            "Failed to remove product from cart."
        );

    } finally {

        setRemovingItemId(null);

    }

};

    // ==========================================
    // PROCEED TO CHECKOUT
    // ==========================================

    const handleCheckout = () => {

        console.log(
            "========== NAVIGATING TO CHECKOUT =========="
        );


        const cartItems =
            cart?.cartItems || [];


        if (cartItems.length === 0) {

            alert(
                "Your cart is empty."
            );

            return;

        }


        navigate("/checkout");

    };


    // ==========================================
    // CONTINUE SHOPPING
    // ==========================================

    const handleContinueShopping = () => {

        navigate("/products");

    };


    // ==========================================
    // LOADING
    // ==========================================

    if (loading) {

        return (

            <div className="container py-5 text-center">

                <div
                    className="spinner-border text-success"
                    role="status"
                />

                <p className="mt-3">

                    Loading your cart...

                </p>

            </div>

        );

    }


    // ==========================================
    // CART DATA
    // ==========================================

    const cartItems =
        cart?.cartItems || [];


    // ==========================================
    // CALCULATE TOTAL
    // ==========================================

    const total = cartItems.reduce(

        (sum, item) => {

            return sum +

                Number(
                    item.subTotal ||
                    item.subtotal ||
                    0
                );

        },

        0

    );


    // ==========================================
    // UI
    // ==========================================

    return (

        <div className="container py-5">


            {/* ======================================
                PAGE HEADER
            ====================================== */}

            <div
                className="
                    d-flex
                    justify-content-between
                    align-items-center
                    mb-4
                "
            >

                <div>

                    <h2 className="fw-bold mb-1">

                        My Cart

                    </h2>


                    <p className="text-muted mb-0">

                        Review your products before checkout

                    </p>

                </div>

            </div>


            <div className="row g-4">


                {/* ==================================
                    CART PRODUCTS
                ================================== */}

                <div className="col-lg-8">

                    <div
                        className="
                            card
                            border-0
                            shadow-sm
                        "
                    >

                        <div className="card-body p-4">


                            <h5 className="fw-bold mb-4">

                                Cart Items

                            </h5>


                            {cartItems.length > 0 ? (

                                cartItems.map(

                                    (item) => (

                                        <div

                                            key={item.id}

                                            className="
                                                d-flex
                                                justify-content-between
                                                align-items-center
                                                border-bottom
                                                py-3
                                            "

                                        >


                                            {/* PRODUCT DETAILS */}

                                            <div>

                                                <h6
                                                    className="
                                                        fw-bold
                                                        mb-1
                                                    "
                                                >

                                                    {
                                                        item.product?.name ||
                                                        item.productName ||
                                                        "Product"
                                                    }

                                                </h6>


                                                <p
                                                    className="
                                                        text-muted
                                                        small
                                                        mb-0
                                                    "
                                                >

                                                    Quantity:{" "}

                                                    {item.quantity}

                                                </p>

                                            </div>


                                            {/* PRICE AND REMOVE */}

                                            <div className="text-end">


                                                <h6
                                                    className="
                                                        fw-bold
                                                        text-success
                                                        mb-2
                                                    "
                                                >

                                                    ₹
                                                    {
                                                        item.subTotal ||
                                                        item.subtotal ||
                                                        0
                                                    }

                                                </h6>


                                                <button

                                                    className="
                                                        btn
                                                        btn-outline-danger
                                                        btn-sm
                                                    "

                                                    disabled={
                                                        removingItemId ===
                                                        item.id
                                                    }

                                                    onClick={() =>
                                                        handleRemoveFromCart(
                                                            item.id
                                                        )
                                                    }

                                                >

                                                    {

                                                        removingItemId ===
                                                        item.id

                                                            ? "Removing..."

                                                            : "Remove"

                                                    }

                                                </button>


                                            </div>


                                        </div>

                                    )

                                )

                            ) : (

                                <div className="text-center py-5">


                                    <h5>

                                        Your cart is empty

                                    </h5>


                                    <p className="text-muted">

                                        Add products to continue shopping.

                                    </p>


                                    <button

                                        className="
                                            btn
                                            btn-success
                                        "

                                        onClick={
                                            handleContinueShopping
                                        }

                                    >

                                        Browse Products

                                    </button>


                                </div>

                            )}

                        </div>

                    </div>

                </div>


                {/* ==================================
                    CART SUMMARY
                ================================== */}

                <div className="col-lg-4">

                    <div

                        className="
                            card
                            border-0
                            shadow-sm
                            sticky-top
                        "

                        style={{

                            top: "100px"

                        }}

                    >

                        <div className="card-body p-4">


                            <h5 className="fw-bold mb-4">

                                Cart Summary

                            </h5>


                            {/* TOTAL ITEMS */}

                            <div
                                className="
                                    d-flex
                                    justify-content-between
                                    mb-3
                                "
                            >

                                <span>

                                    Total Items

                                </span>


                                <strong>

                                    {cartItems.length}

                                </strong>

                            </div>


                            {/* TOTAL AMOUNT */}

                            <div
                                className="
                                    d-flex
                                    justify-content-between
                                    mb-3
                                "
                            >

                                <span>

                                    Total

                                </span>


                                <strong className="text-success">

                                    ₹{total}

                                </strong>

                            </div>


                            <hr />


                            {/* CHECKOUT BUTTON */}

                            <button

                                className="
                                    btn
                                    btn-success
                                    w-100
                                    mb-3
                                "

                                disabled={
                                    cartItems.length === 0
                                }

                                onClick={
                                    handleCheckout
                                }

                            >

                                Proceed to Checkout

                            </button>


                            {/* CONTINUE SHOPPING */}

                            <button

                                className="
                                    btn
                                    btn-outline-secondary
                                    w-100
                                "

                                onClick={
                                    handleContinueShopping
                                }

                            >

                                ← Continue Shopping

                            </button>


                        </div>

                    </div>

                </div>


            </div>


        </div>

    );

};


export default Cart;