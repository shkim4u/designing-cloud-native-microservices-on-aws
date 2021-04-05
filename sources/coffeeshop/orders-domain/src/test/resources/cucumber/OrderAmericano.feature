Feature: 매장에서 커피를 주문한다.

  Scenario: 커피숍의 손님으로서, 매장에 머무르면서 커피를 마실 수 있도록, 커피를 주문하고 싶다.
    Given 손님이 아래의 주문 내역으로 커피를 주문한다.
      | coffee    | quantity | price |
      | Americano | 2        | 80    |
    When 주문이 확인된다.
    Then 전체 주문 금액은 160l이다.