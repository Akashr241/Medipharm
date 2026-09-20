import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getMyOrders } from "../services/orderService.js";

function Orders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    loadOrders();
  }, []);

  const loadOrders = async () => {
    try {
      const response = await getMyOrders();

      console.log("My Orders:", response);
      setOrders(response);
    } catch (error) {
      console.log("Error loading orders:", error);
      setError("Unable to load your orders.");
    } finally {
      setLoading(false);
    }
  };

  /*
   * Helper function for status styling
   */
  const getStatusBadge = (status) => {
    const s = (status || "").toLowerCase();
    if (s.includes("deliver") || s.includes("complet") || s.includes("paid")) {
      return "bg-success-subtle text-success border border-success-subtle";
    }
    if (s.includes("pend") || s.includes("process")) {
      return "bg-warning-subtle text-warning-emphasis border border-warning-subtle";
    }
    if (s.includes("cancel") || s.includes("fail")) {
      return "bg-danger-subtle text-danger border border-danger-subtle";
    }
    return "bg-primary-subtle text-primary border border-primary-subtle";
  };

  if (loading) {
    return (
      <div
        className="min-vh-100 bg-light d-flex justify-content-center align-items-center"
        style={{ paddingTop: "90px" }}
      >
        <div className="text-center">
          <div
            className="spinner-border text-success"
            style={{ width: "3rem", height: "3rem" }}
            role="status"
          />
          <p className="text-muted mt-3 fw-medium">Loading your orders...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container py-5 min-vh-100" style={{ paddingTop: "100px" }}>
        <div className="alert alert-danger rounded-4 d-flex align-items-center gap-3 p-4 shadow-sm" role="alert">
          <svg width="24" height="24" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
          <div>
            <h6 className="fw-bold mb-1">Error Occurred</h6>
            <span className="small mb-0">{error}</span>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-light min-vh-100 pb-5" style={{ paddingTop: "90px" }}>
      <div className="container" style={{ maxWidth: "900px" }}>

        {/* Page Header */}
        <div className="d-flex flex-column flex-sm-row justify-content-between align-items-sm-center gap-3 mb-4">
          <div>
            <div className="d-inline-flex align-items-center gap-2 px-3 py-1 rounded-pill bg-success-subtle text-success text-xs font-semibold mb-2">
              <span className="p-1 bg-success rounded-circle"></span>
              Order History
            </div>
            <h1 className="fw-bold text-dark mb-1">My Orders</h1>
            <p className="text-muted small mb-0">Track and manage your past pharmacy orders</p>
          </div>
          <div className="text-muted small">
            Total Orders: <span className="fw-bold text-dark">{orders.length}</span>
          </div>
        </div>

        {/* Empty State */}
        {orders.length === 0 ? (
          <div className="card border-0 shadow-sm rounded-4 p-5 text-center my-4">
            <div className="py-4">
              <div
                className="d-inline-flex align-items-center justify-content-center bg-light text-muted rounded-circle mb-3"
                style={{ width: "80px", height: "80px" }}
              >
                <svg width="40" height="40" fill="none" stroke="currentColor" strokeWidth="1.75" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
                </svg>
              </div>
              <h4 className="fw-bold text-dark">No orders found</h4>
              <p className="text-muted mx-auto" style={{ maxWidth: "350px" }}>
                You haven't placed any orders yet. Browse our medicines and get healthcare essentials delivered.
              </p>
              <Link to="/products" className="btn btn-success rounded-pill px-4 py-2 mt-2 fw-semibold">
                Start Shopping
              </Link>
            </div>
          </div>
        ) : (
          /* Orders List */
          <div className="d-flex flex-column gap-3">
            {orders.map((order) => (
              <div
                className="card border-0 shadow-sm rounded-4 hover-shadow transition-all overflow-hidden"
                key={order.id}
              >
                <div className="card-body p-4">
                  <div className="row align-items-center gy-3">

                    {/* Order Icon & Number */}
                    <div className="col-md-5">
                      <div className="d-flex align-items-center gap-3">
                        <div
                          className="bg-success-subtle text-success rounded-3 d-flex align-items-center justify-content-center flex-shrink-0"
                          style={{ width: "48px", height: "48px" }}
                        >
                          <svg width="24" height="24" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                          </svg>
                        </div>
                        <div>
                          <div className="small text-muted">Reference ID</div>
                          <h6 className="fw-bold text-dark mb-0">Order #{order.id}</h6>
                        </div>
                      </div>
                    </div>

                    {/* Total Price */}
                    <div className="col-6 col-md-2">
                      <div className="small text-muted">Total Amount</div>
                      <div className="fw-bold text-dark fs-5">
                        ₹{order.totalPrice || order.totalAmount || 0}
                      </div>
                    </div>

                    {/* Status Badge */}
                    <div className="col-6 col-md-2 text-md-center">
                      <div className="small text-muted d-md-none">Status</div>
                      <span className={`badge rounded-pill px-3 py-2 fw-semibold ${getStatusBadge(order.status)}`}>
                        {order.status || "Processing"}
                      </span>
                    </div>

                    {/* View Details CTA */}
                    <div className="col-md-3 text-end">
                      <Link
                        to={`/orders/${order.id}`}
                        className="btn btn-outline-success rounded-pill px-3 py-2 fw-semibold d-inline-flex align-items-center gap-2 text-decoration-none w-100 justify-content-center"
                      >
                        <span>View Order</span>
                        <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2.5" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
                        </svg>
                      </Link>
                    </div>

                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

      </div>
    </div>
  );
}

export default Orders;
