package cn.lmjia.market.core.repository.deal;

import cn.lmjia.market.core.entity.Login;
import cn.lmjia.market.core.entity.deal.AgentLevel;
import cn.lmjia.market.core.util.AbstractAgentLevelRepository;

/**
 * @author CJ
 */
public interface AgentLevelRepository extends AbstractAgentLevelRepository<AgentLevel> {

    AgentLevel findTopByLoginAndLevel(Login login, int level);

}