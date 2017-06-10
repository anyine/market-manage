package cn.lmjia.market.dealer.controller.order;

import cn.lmjia.market.core.entity.Login;
import cn.lmjia.market.core.entity.MainOrder;
import cn.lmjia.market.core.entity.support.OrderStatus;
import cn.lmjia.market.core.row.RowCustom;
import cn.lmjia.market.core.row.RowDefinition;
import cn.lmjia.market.core.row.supplier.JQueryDataTableDramatizer;
import cn.lmjia.market.core.rows.MainOrderRows;
import cn.lmjia.market.core.service.MainOrderService;
import cn.lmjia.market.core.service.ReadService;
import cn.lmjia.market.core.util.ApiDramatizer;
import cn.lmjia.market.dealer.service.AgentService;
import me.jiangcai.lib.spring.data.AndSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * 订单相关的数据服务
 *
 * @author CJ
 */
@Controller
public class OrderDataController {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d", Locale.CHINA);
    @Autowired
    private ReadService readService;
    @Autowired
    private AgentService agentService;
    @Autowired
    private MainOrderService mainOrderService;
    @Autowired
    private ConversionService conversionService;

    /**
     * @return 仅仅显示我的订单
     */
    @RequestMapping(method = RequestMethod.GET, value = "/api/orderList")
    @RowCustom(distinct = true, dramatizer = ApiDramatizer.class)
    public RowDefinition myOrder(@AuthenticationPrincipal Login login, String search, OrderStatus status) {
        return new MainOrderRows(login, t -> t.format(formatter)) {
            @Override
            public Specification<MainOrder> specification() {
                return new AndSpecification<>(
                        mainOrderService.search(search, status)
                        , (root, query, cb) -> cb.equal(root.get("orderBy"), login)
                );
            }
        };
    }

    /**
     * 仅仅处理自己可以管辖的订单
     * 即属于我方代理体系的
     */
    @RequestMapping(method = RequestMethod.GET, value = "/orderData/manageableList")
    @RowCustom(distinct = true, dramatizer = JQueryDataTableDramatizer.class)
    public RowDefinition manageableList(@AuthenticationPrincipal Login login, String orderId
            , @RequestParam(value = "phone", required = false) String mobile, Long goodId
            , @DateTimeFormat(pattern = "yyyy-M-d") @RequestParam(required = false) LocalDate orderDate
            , OrderStatus status) {
        return new MainOrderRows(login, t -> conversionService.convert(t, String.class)) {
            @Override
            public Specification<MainOrder> specification() {
                return new AndSpecification<>(
                        mainOrderService.search(orderId, mobile, goodId, orderDate, status)
                        , agentService.manageableOrder(login)
                );
            }
        };
    }

}
