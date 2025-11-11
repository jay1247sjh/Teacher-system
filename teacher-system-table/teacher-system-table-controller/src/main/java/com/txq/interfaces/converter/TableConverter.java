package com.txq.interfaces.converter;

import com.txq.application.entity.query.TableQuery;
import com.txq.interfaces.dto.TableDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 转换类
 */
@Mapper
public interface TableConverter {

    TableConverter INSTANCE = Mappers.getMapper(TableConverter.class);

    TableQuery toQuery(TableDTO tableDTO);
}
