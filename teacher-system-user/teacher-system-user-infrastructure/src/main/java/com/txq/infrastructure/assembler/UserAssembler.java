package com.txq.infrastructure.assembler;

import com.txq.domain.model.Email;
import com.txq.domain.model.Password;
import com.txq.domain.model.User;
import com.txq.domain.model.WorkId;
import com.txq.infrastructure.po.UserPO;

/**
 * 转换器
 */
public class UserAssembler {

    public static UserPO toPO(User user) {
        UserPO po = new UserPO();
        po.setId(user.getWorkId().getId());
        po.setUsername(user.getUsername());
        po.setPassword(user.getPassword().getPassword());
        po.setEmail(user.getEmail().getEmail());
        return po;
    }

    public static User toDomain(UserPO po) {
        return new User(
                WorkId.of(po.getId()),
                po.getUsername(),
                Password.encrypted(po.getPassword()),
                Email.of(po.getEmail())
        );
    }
}