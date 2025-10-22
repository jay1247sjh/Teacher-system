package com.txq.interfaces.converter;

import com.txq.application.query.EmailQuery;
import com.txq.application.query.UserQuery;
import com.txq.interfaces.entity.dto.EmailDTO;
import com.txq.interfaces.entity.dto.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 转换类
 */
@Mapper
public interface UserConverter {

    UserConverter INSTANCE = Mappers.getMapper(UserConverter.class);

    UserQuery toQuery(UserDTO userDTO);

    EmailQuery toQuery(EmailDTO emailDTO);
}
