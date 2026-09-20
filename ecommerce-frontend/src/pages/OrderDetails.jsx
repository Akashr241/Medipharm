import React, { useEffect, useState } from "react";

import { useParams, Link } from "react-router-dom";

import { getOrderById } from "../services/orderService.js";



function OrderDetails() {



  const { orderId } = useParams();



  const [order, setOrder] = useState(null);

  const [loading, setLoading] = useState(true);

  const [error, setError] = useState("");





  // ==========================================

  // LOAD ORDER DETAILS

  // ==========================================



  useEffect(() => {



    const loadOrder = async () => {



      try {



        setLoading(true);

        setError("");



        const response = await getOrderById(orderId);





        console.log(

          "========== ORDER DETAILS =========="

        );



        console.log(

          "Order Details:",

          response

        );





        setOrder(response);



      } catch (error) {



        console.error(

          "Order details error:",

          error

        );





        setError(

          "Unable to load order details."

        );



      } finally {



        setLoading(false);



      }



    };





    loadOrder();



  }, [orderId]);





  // ==========================================

  // STATUS BADGE

  // ==========================================



  const getStatusBadge = (status) => {



    const s =

      (status || "").toLowerCase();





    if (

      s.includes("deliver") ||

      s.includes("complet") ||

      s.includes("paid")

    ) {



      return "bg-success-subtle text-success border border-success-subtle";



    }





    if (

      s.includes("pend") ||

      s.includes("process")

    ) {



      return "bg-warning-subtle text-warning-emphasis border border-warning-subtle";



    }





    if (

      s.includes("cancel") ||

      s.includes("fail")

    ) {



      return "bg-danger-subtle text-danger border border-danger-subtle";



    }





    return "bg-primary-subtle text-primary border border-primary-subtle";

  };





  // ==========================================

  // LOADING SCREEN

  // ==========================================



  if (loading) {



    return (



      <div

        className="min-vh-100 bg-light d-flex justify-content-center align-items-center"

        style={{

          paddingTop: "90px"

        }}

      >



        <div className="text-center">



          <div

            className="spinner-border text-success"

            style={{

              width: "3rem",

              height: "3rem"

            }}

            role="status"

          />



          <p className="text-muted mt-3 fw-medium">

            Loading order details...

          </p>



        </div>



      </div>



    );

  }





  // ==========================================

  // ERROR / ORDER NOT FOUND

  // ==========================================



  if (error || !order) {



    return (



      <div

        className="container py-5 min-vh-100"

        style={{

          paddingTop: "100px"

        }}

      >



        <div

          className="alert alert-danger rounded-4 d-flex align-items-center gap-3 p-4 shadow-sm mb-4"

          role="alert"

        >



          <svg

            width="24"

            height="24"

            fill="none"

            stroke="currentColor"

            strokeWidth="2"

            viewBox="0 0 24 24"

          >



            <circle

              cx="12"

              cy="12"

              r="10"

            />



            <line

              x1="12"

              y1="8"

              x2="12"

              y2="12"

            />



            <line

              x1="12"

              y1="16"

              x2="12.01"

              y2="16"

            />



          </svg>





          <div>



            <h6 className="fw-bold mb-1">

              Notice

            </h6>



            <span className="small mb-0">

              {error || "Order not found."}

            </span>



          </div>



        </div>





        <Link

          to="/orders"

          className="btn btn-outline-success rounded-pill px-4"

        >

          ← Back to Orders

        </Link>



      </div>

    );

  }





  // ==========================================

  // MAIN ORDER DETAILS PAGE

  // ==========================================



  return (



    <div

      className="bg-light min-vh-100 pb-5"

      style={{

        paddingTop: "90px"

      }}

    >



      <div

        className="container"

        style={{

          maxWidth: "850px"

        }}

      >





        {/* ======================================

            BACK LINK

        ====================================== */}



        <Link

          to="/orders"

          className="text-success text-decoration-none fw-semibold d-inline-flex align-items-center gap-2 mb-4"

        >



          <svg

            width="18"

            height="18"

            fill="none"

            stroke="currentColor"

            strokeWidth="2.5"

            viewBox="0 0 24 24"

          >



            <path

              strokeLinecap="round"

              strokeLinejoin="round"

              d="M10 19l-7-7m0 0l7-7m-7 7h18"

            />



          </svg>



          Back to My Orders



        </Link>





        {/* ======================================

            ORDER HEADER CARD

        ====================================== */}



        <div

          className="card border-0 shadow-sm rounded-4 mb-4 overflow-hidden"

        >



          <div className="card-body p-4 p-md-5">





            {/* ==================================

                ORDER HEADER

            ================================== */}



            <div

              className="

                d-flex

                flex-column

                flex-sm-row

                justify-content-between

                align-items-sm-center

                gap-3

                border-bottom

                pb-4

                mb-4

              "

            >



              <div>



                <span

                  className="

                    badge

                    bg-light

                    text-muted

                    border

                    px-2

                    py-1

                    rounded-2

                    mb-2

                  "

                >

                  Invoice Receipt

                </span>





                <h2 className="fw-bold text-dark mb-1">



                  Order #{order.id}



                </h2>





                <p

                  className="

                    text-muted

                    small

                    mb-0

                    d-flex

                    align-items-center

                    gap-2

                  "

                >



                  <svg

                    width="16"

                    height="16"

                    fill="none"

                    stroke="currentColor"

                    strokeWidth="2"

                    viewBox="0 0 24 24"

                  >



                    <rect

                      x="3"

                      y="4"

                      width="18"

                      height="18"

                      rx="2"

                    />



                    <line

                      x1="16"

                      y1="2"

                      x2="16"

                      y2="6"

                    />



                    <line

                      x1="8"

                      y1="2"

                      x2="8"

                      y2="6"

                    />



                    <line

                      x1="3"

                      y1="10"

                      x2="21"

                      y2="10"

                    />



                  </svg>





                  {order.orderDate

                    ? new Date(

                        order.orderDate

                      ).toLocaleString(

                        "en-US",

                        {

                          dateStyle: "medium",

                          timeStyle: "short"

                        }

                      )

                    : "Recent Order"}



                </p>



              </div>





              {/* STATUS */}



              <div className="text-sm-end">



                <span

                  className={`

                    badge

                    rounded-pill

                    px-3

                    py-2

                    fs-6

                    fw-semibold

                    ${getStatusBadge(order.status)}

                  `}

                >



                  {order.status || "Completed"}



                </span>



              </div>



            </div>





            {/* ==================================

                ORDER ITEMS TITLE

            ================================== */}



            <h5

              className="

                fw-bold

                text-dark

                mb-3

                d-flex

                align-items-center

                gap-2

              "

            >



              <svg

                width="20"

                height="20"

                className="text-success"

                fill="none"

                stroke="currentColor"

                strokeWidth="2"

                viewBox="0 0 24 24"

              >



                <path

                  strokeLinecap="round"

                  strokeLinejoin="round"

                  d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"

                />



              </svg>





              Order Items (

              {order.orderItems?.length || 0}

              )



            </h5>





            {/* ==================================

                ORDER ITEMS

            ================================== */}



            <div

              className="

                d-flex

                flex-column

                gap-3

                mb-4

              "

            >



              {order.orderItems?.map(

                (item) => (



                  <div

                    key={item.id}

                    className="

                      p-3

                      bg-light

                      rounded-3

                      d-flex

                      flex-column

                      flex-sm-row

                      justify-content-between

                      align-items-sm-center

                      gap-3

                      border

                      border-light-subtle

                    "

                  >





                    {/* PRODUCT INFORMATION */}



                    <div

                      className="

                        d-flex

                        align-items-center

                        gap-3

                      "

                    >



                      <div

                        className="

                          rounded-3

                          bg-white

                          text-success

                          d-flex

                          align-items-center

                          justify-content-center

                          shadow-sm

                          border

                        "

                        style={{

                          width: "42px",

                          height: "42px"

                        }}

                      >



                        <svg

                          width="20"

                          height="20"

                          fill="none"

                          stroke="currentColor"

                          strokeWidth="2"

                          viewBox="0 0 24 24"

                        >



                          <path

                            strokeLinecap="round"

                            strokeLinejoin="round"

                            d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z"

                          />



                        </svg>



                      </div>





                      <div>



                        <h6 className="fw-bold text-dark mb-0">



                          {item.productName}



                        </h6>





                        <small className="text-muted">



                          Unit Price: ₹{item.price}



                        </small>



                      </div>



                    </div>





                    {/* QUANTITY + SUBTOTAL */}



                    <div

                      className="

                        d-flex

                        align-items-center

                        justify-content-between

                        justify-content-sm-end

                        gap-4

                      "

                    >



                      <div className="text-sm-center">



                        <span className="small text-muted d-block">

                          Qty

                        </span>





                        <span

                          className="

                            badge

                            bg-white

                            text-dark

                            border

                            px-2

                            py-1

                            fw-bold

                          "

                        >



                          × {item.quantity}



                        </span>



                      </div>





                      <div

                        className="text-end"

                        style={{

                          minWidth: "90px"

                        }}

                      >



                        <span className="small text-muted d-block">

                          Subtotal

                        </span>





                        <span

                          className="

                            fw-bold

                            text-dark

                            fs-6

                          "

                        >



                          ₹{item.subtotal}



                        </span>



                      </div>



                    </div>



                  </div>

                )

              )}



            </div>





            {/* ==================================

                GRAND TOTAL

            ================================== */}



            <div

              className="

                p-4

                bg-success-subtle

                rounded-4

                border

                border-success-subtle

                d-flex

                justify-content-between

                align-items-center

              "

            >



              <div>



                <span

                  className="

                    text-success

                    fw-semibold

                    small

                    text-uppercase

                    tracking-wider

                    d-block

                  "

                >

                  Grand Total

                </span>





                <span className="text-muted small">



                  Includes all applicable pharmacy taxes



                </span>



              </div>





              <h3 className="fw-bold text-success mb-0">



                ₹

                {order.totalAmount ||

                  order.totalPrice}



              </h3>



            </div>



          </div>



        </div>





        {/* ======================================

            BACK TO ORDERS BUTTON

        ====================================== */}



        <div className="text-center">



          <Link

            to="/orders"

            className="

              btn

              btn-outline-success

              rounded-pill

              px-4

              py-2

              fw-semibold

              d-inline-flex

              align-items-center

              gap-2

            "

          >



            <svg

              width="18"

              height="18"

              fill="none"

              stroke="currentColor"

              strokeWidth="2.5"

              viewBox="0 0 24 24"

            >



              <path

                strokeLinecap="round"

                strokeLinejoin="round"

                d="M10 19l-7-7m0 0l7-7m-7 7h18"

              />



            </svg>



            Back to Orders List



          </Link>



        </div>
      </div>
    </div>


  );

}
export default OrderDetails;