package cucumber;

// import io.cucumber.java.En;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import solid.humank.coffeeshop.order.commands.CreateOrder;
import solid.humank.coffeeshop.order.exceptions.AggregateException;
import solid.humank.coffeeshop.order.models.Order;
import solid.humank.coffeeshop.order.models.OrderId;
import solid.humank.coffeeshop.order.models.OrderItem;
import solid.humank.coffeeshop.order.models.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderAmericanoSteps {

	CreateOrder cmd;
	Order createdOrder;

	@Given("손님이 아래의 주문 내역으로 커피를 주문한다.")
	public void customer_wants_to_order_coffee(io.cucumber.datatable.DataTable dataTable) {
		List<Map<String, String>> testData = dataTable.asMaps(String.class, String.class);
		Map<String, String> sample = testData.get(0);

		String productId = sample.get("coffee");
		int qty = Integer.valueOf(sample.get("quantity"));
		BigDecimal price = new BigDecimal(sample.get("price"));

		List<OrderItem> items = new ArrayList<>();
		items.add(new OrderItem(productId, qty, price));
		cmd = new CreateOrder(new OrderId(1, OffsetDateTime.now()), "0", OrderStatus.INITIAL, items);

	}

	@When("주문이 확인된다.")
	public void the_order_is_confirmed() throws AggregateException {
		createdOrder = Order.create(cmd);
	}

	@Then("전체 주문 금액은 {int}l이다.")
	public void the_total_fee_should_be_160l(Integer int1) {
		System.out.println("Total fee: " + int1);
		assertEquals(createdOrder.totalFee().longValue(), int1.longValue());
	}

}
