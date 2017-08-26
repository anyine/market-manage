package cn.lmjia.market.core.service.impl;

import cn.lmjia.market.core.entity.MainGood;
import cn.lmjia.market.core.entity.MainGood_;
import cn.lmjia.market.core.entity.channel.Channel;
import cn.lmjia.market.core.entity.channel.Channel_;
import cn.lmjia.market.core.repository.MainGoodRepository;
import cn.lmjia.market.core.service.MainGoodService;
import cn.lmjia.market.core.service.MainOrderService;
import me.jiangcai.logistics.entity.Product_;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author CJ
 */
@Service("mainGoodService")
public class MainGoodServiceImpl implements MainGoodService {
    @Autowired
    private MainGoodRepository mainGoodRepository;
    @Autowired
    private MainOrderService mainOrderService;
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private EntityManager entityManager;

    @Override
    public List<MainGood> forSale(Channel channel) {
        List<MainGood> mainGoodList;
        if (channel == null)
            mainGoodList = mainGoodRepository.findAll((root, query, cb) -> {
                Join<MainGood, Channel> channelJoin = root.join(MainGood_.channel, JoinType.LEFT);
                return cb.and(
                        cb.isTrue(root.get(MainGood_.enable))
                        , cb.isTrue(root.get(MainGood_.product).get(Product_.enable))
                        , cb.or(
                                channelJoin.isNull()
                                , cb.isFalse(channelJoin.get(Channel_.extra))
                        )
                );
            });
        else
            mainGoodList = mainGoodRepository.findAll((root, query, cb) -> cb.and(
                    cb.isTrue(root.get(MainGood_.enable))
                    , cb.isTrue(root.get(MainGood_.product).get(Product_.enable))
                    , cb.equal(root.get(MainGood_.channel), channel)
            ));
        if (mainGoodList != null && mainGoodList.size() > 0) {
            mainOrderService.calculateGoodStock(mainGoodList);
        }
        return mainGoodList;
    }

    @Override
    public List<MainGood> forSale() {
        return forSale(null);
    }

    @Override
    public void priceCheck() {
        mainGoodRepository.findAll().forEach(good -> {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<BigDecimal> priceQuery = cb.createQuery(BigDecimal.class);
            Root<MainGood> root = priceQuery.from(MainGood.class);

            BigDecimal value = entityManager.createQuery(priceQuery.select(MainGood.getTotalPrice(root, cb))
                    .where(cb.equal(root, good)))
                    .getSingleResult();
            assert value.equals(good.getTotalPrice());
        });
    }
}
