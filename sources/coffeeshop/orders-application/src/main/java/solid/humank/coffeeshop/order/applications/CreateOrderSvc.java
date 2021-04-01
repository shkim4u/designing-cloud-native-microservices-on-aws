package solid.humank.coffeeshop.order.applications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solid.humank.coffeeshop.infra.serializations.DomainEventPublisher;
import solid.humank.coffeeshop.order.commands.CreateOrder;
import solid.humank.coffeeshop.order.datacontracts.messages.CreateOrderMsg;
import solid.humank.coffeeshop.order.datacontracts.results.OrderItemRst;
import solid.humank.coffeeshop.order.datacontracts.results.OrderRst;
import solid.humank.coffeeshop.order.exceptions.AggregateException;
import solid.humank.coffeeshop.order.interfaces.IOrderRepository;
import solid.humank.coffeeshop.order.models.Order;
import solid.humank.coffeeshop.order.models.OrderId;
import solid.humank.coffeeshop.order.models.OrderItem;
import solid.humank.coffeeshop.order.models.OrderStatus;
import solid.humank.ddd.commons.interfaces.ITranslator;
import solid.humank.ddd.commons.utilities.DomainModelMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@ApplicationScoped
public class CreateOrderSvc implements Serializable {

	// TODO confirmation result
	@Inject
	public IOrderRepository repository;
	@Inject
	public ITranslator<List<OrderItem>, List<OrderItemRst>> translator;
	@Inject
	DomainEventPublisher domainEventPublisher;
	Logger logger = LoggerFactory.getLogger(CreateOrderSvc.class);

	/**
	 * The barista will accept the order and start to get the raw materials from the
	 * refrigerator according to the products on the order The barista will update
	 * the order status regularly
	 * <p>
	 * So the relationship between the barista and the BC of the order is a Partner
	 * Orders will not directly affect inventory Where are you writing here? But
	 * when the barista fetches the refrigerator, if it is already insufficient, he
	 * will simultaneously fetch/deduct the inventory
	 * <p>
	 * Producer --> Event <-- Consumer OrderDomain |OrderCreated | Coffee to accept
	 * the order Coffee |OrderAccepted|
	 */
	public CreateOrderSvc() {
	}

	public OrderRst establishOrder(CreateOrderMsg request) throws AggregateException {

		OrderId id = this.repository.generateOrderId();
		List<OrderItem> items = translator.translate(request.getItems());

		CreateOrder cmd = new CreateOrder(id, request.getTableNo(), OrderStatus.INITIAL, items);
		Order createdOrder = Order.create(cmd);

		logger.info(new DomainModelMapper().writeToJsonString(createdOrder));

		this.repository.save(createdOrder);

		domainEventPublisher.publish(createdOrder.getDomainEvents());

		return new OrderRst(createdOrder);
	}
}
