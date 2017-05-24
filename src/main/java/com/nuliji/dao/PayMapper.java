package com.nuliji.dao;

import com.nuliji.proj.Pay;

public interface PayMapper {
    int deleteByPrimaryKey(String id);

    int insert(Pay record);

    int insertSelective(Pay record);

    Pay selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Pay record);

    int updateByPrimaryKey(Pay record);
}