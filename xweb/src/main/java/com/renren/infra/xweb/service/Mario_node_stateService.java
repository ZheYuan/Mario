
package com.renren.infra.xweb.service;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.renren.infra.xweb.entity.Mario_node_state;
import com.renren.infra.xweb.repository.Mario_node_stateMybatisDao;

@Component
@Transactional
public class Mario_node_stateService {

    @Autowired
    private Mario_node_stateMybatisDao mario_node_stateDao;

    /**
     * 创建分页查询.
     * 
     * @param filterParams
     * @param pageNumber
     * @param pageSize
     * @param sortType
     * @return Page<Test>
     */
    public Page<Mario_node_state> getMario_node_state(Map<String, Object> filterParams, int pageNumber, int pageSize) {

        List<Mario_node_state> mario_node_states = mario_node_stateDao.find(filterParams, buildRowBounds(pageNumber, pageSize));
        int totalnum = mario_node_stateDao.findTotalNum(filterParams);

        return new PageImpl<Mario_node_state>(mario_node_states, new PageRequest(pageNumber - 1, pageSize), totalnum);
    }

    /**
     * 查找所有的Mario_node_state.
     * 
     * @return List<Mario_node_state>
     */
    public List<Mario_node_state> getAllMario_node_state() {
        return mario_node_stateDao.findAll();
    }

    /**
     * 创建分页请求.
     * 
     * @param pageNumber
     * @param pageSize
     * @return RowBounds
     */
    private RowBounds buildRowBounds(int pageNumber, int pageSize) {
        return new RowBounds((pageNumber - 1) * pageSize, pageSize);
    }

    /**
     * 保存Mario_node_state.
     * 
     * @param newMario_node_state
     */
    public void saveMario_node_state(Mario_node_state newMario_node_state) {
        if (newMario_node_state.getid() != null     ) {
            mario_node_stateDao.update(newMario_node_state);
        } else {
            mario_node_stateDao.insert(newMario_node_state);
        }
    }

    /**
     * 根据主键获取Mario_node_state
     * 
     * @param id
     * @return Mario_node_state
     */
    public Mario_node_state getMario_node_state(Integer id    ) {
        return mario_node_stateDao.findById	(id);
    }

    /**
     * 根据主键删除Mario_node_state
     * 
     * @param id
     */
    public void deleteMario_node_state(Integer id) {
        mario_node_stateDao.delete(id);
    }

}
