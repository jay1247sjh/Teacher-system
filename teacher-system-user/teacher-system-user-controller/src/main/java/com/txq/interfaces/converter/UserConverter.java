package com.txq.interfaces.converter;

import com.txq.application.command.UserCommand;
import com.txq.interfaces.entity.dto.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 转换类
 * */
@Mapper
public interface UserConverter {

    UserConverter INSTANCE = Mappers.getMapper(UserConverter.class);

    UserCommand toCommand(UserDTO userDTO);
}
