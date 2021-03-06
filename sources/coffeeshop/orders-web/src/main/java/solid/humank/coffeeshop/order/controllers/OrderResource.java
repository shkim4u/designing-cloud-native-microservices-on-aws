package solid.humank.coffeeshop.order.controllers;

import solid.humank.coffeeshop.order.applications.CreateOrderSvc;
import solid.humank.coffeeshop.order.datacontracts.messages.CreateOrderMsg;
import solid.humank.coffeeshop.order.datacontracts.results.OrderItemRst;
import solid.humank.coffeeshop.order.datacontracts.results.OrderRst;
import solid.humank.coffeeshop.order.exceptions.AggregateException;
import solid.humank.coffeeshop.order.models.requests.AddOrderReq;
import solid.humank.coffeeshop.order.models.requestsmodels.OrderItemRM;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Path("/order")
public class OrderResource {

	Logger logger = LoggerFactory.getLogger(OrderResource.class);

	@Inject
	CreateOrderSvc service;

	public OrderResource() {
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createOrder(AddOrderReq request) {
		/**
		 * [2021-03-23] KSH: Testing how Quarkus generates GeneratedMain class and calls
		 * into this resource class.
		 */
		StackTraceElement[] stacks = (new Throwable()).getStackTrace();
		// StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		for (StackTraceElement element : stacks) {
			logger.info("" + element);
		}
		logger.info("request: " + request.toString());

		CreateOrderMsg cmd = new CreateOrderMsg("0", this.transformToOrderItemVM(request.getItems()));

		OrderRst orderRst = null;
		String err = null;

		try {
			orderRst = service.establishOrder(cmd);
		} catch (AggregateException e) {
			e.printStackTrace();
			err = e.getMessage();
		}

		if (err == null) {
			return Response.ok(orderRst).status(Response.Status.CREATED).build();
		}
		return Response.ok(err).build();
	}

	private List<OrderItemRst> transformToOrderItemVM(List<OrderItemRM> items) {
		List<OrderItemRst> result = new ArrayList<>();
		items.forEach(orderItemRM -> {
			result.add(new OrderItemRst(orderItemRM.getProductId(), orderItemRM.getQty(), orderItemRM.getPrice()));
		});

		return result;
	}
}
