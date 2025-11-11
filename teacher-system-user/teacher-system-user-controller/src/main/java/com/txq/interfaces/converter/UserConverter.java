package com.txq.interfaces.converter;

import com.txq.application.entity.query.EmailQuery;
import com.txq.application.entity.query.LoginQuery;
import com.txq.application.entity.query.UserQuery;
import com.txq.interfaces.dto.EmailDTO;
import com.txq.interfaces.dto.LoginDTO;
import com.txq.interfaces.dto.UserDTO;
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

    LoginQuery toQuery(LoginDTO loginDTO);
}
