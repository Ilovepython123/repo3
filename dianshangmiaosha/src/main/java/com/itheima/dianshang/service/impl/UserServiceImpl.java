package com.itheima.dianshang.service.impl;

import com.itheima.dianshang.dao.UserDOMapper;
import com.itheima.dianshang.dao.UserPasswordDOMapper;
import com.itheima.dianshang.dataobject.UserDO;
import com.itheima.dianshang.dataobject.UserPasswordDO;
import com.itheima.dianshang.error.BusinessException;
import com.itheima.dianshang.error.EmBussinessError;
import com.itheima.dianshang.service.UserService;
import com.itheima.dianshang.service.model.UserModel;
import com.itheima.dianshang.validator.ValidationResult;
import com.itheima.dianshang.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Autowired
    private ValidatorImpl validatorImpl;



    @Override
    public UserModel getUserById(Integer id) {
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        //用model层将密码和用户的信息联系到一起
        if (userDO == null) {
            return null;
        }
        //通过userId关联查询
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO,userModel);
        userModel.setPassword(userPasswordDO.getEncrptPassword());
        return userModel;
    }

    //登录的方法
    public UserModel validateLogin(String telphone, String enPassword) throws BusinessException {
        //根据telphone得到user_info表中的一条记录，根据这条记录的主键，查询到user_password表中的相关联的一条记录
        UserDO userDO = userDOMapper.selectByTelphone(telphone);

        //如果该手机号还没有注册过，应该抛出一个异常
        if (userDO == null) {
            throw new BusinessException(EmBussinessError.USER_LOGIN_FAIL);
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        //判断输入的密码和从数据表中得到的密码是否一致，不一致就抛出异常
        if (!enPassword.equals(userPasswordDO.getEncrptPassword())) {
            throw new BusinessException(EmBussinessError.USER_LOGIN_FAIL);
        }
        //将两个数据库实体类转换成一个pojo类
        UserModel userModel = convertFromDataObjcet(userDO, userPasswordDO);
        return userModel;
    }



    public void registers(UserModel userModel) throws BusinessException {

        /*
        注册时的各个字段要进行校验
         */
        ValidationResult validationResult = validatorImpl.validate(userModel);
        //有不符合校验的字段
        if (validationResult.isHasErroes()) {
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, validationResult.getErrMsg());
        }

        //一个手机只能注册一个用户 tel 对应唯一user
        UserDO userDO = convertUerDaoFromUserModel(userModel);
        try {
            userDOMapper.insertSelective(userDO);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "手机号已被注册");
        }
        userModel.setId(userDO.getId());
        UserPasswordDO userPasswordDO = convertUserPassFromUserModel(userModel);
        /*
        dao 和pass这两个事务同时成功，必须加上@Transactional
         */
        userPasswordDOMapper.insertSelective(userPasswordDO);
    }

    //将userModel转化为userDo
    public UserDO convertUerDaoFromUserModel(UserModel userModel) throws BusinessException {

        if (userModel == null) {
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR);
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel, userDO);
        return userDO;
    }


    //将userModel转化为userPassword
    public UserPasswordDO convertUserPassFromUserModel(UserModel userModel) throws BusinessException {
        if (userModel == null) {
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR);
        }
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncrptPassword(userPasswordDO.getEncrptPassword());
        //通过userId关联查询
        userPasswordDO.setUserId(userModel.getId());
        BeanUtils.copyProperties(userModel, userPasswordDO);
        return userPasswordDO;
    }

    /*
    将数据库中的dao映射成业务逻辑model
     */
    public UserModel convertFromDataObjcet(UserDO userDO, UserPasswordDO userPasswordDO) {
        if (userDO == null) {
            return null;
        }
        UserModel userModel = new UserModel();

        //将userDao对象转化位userModel对象
        BeanUtils.copyProperties(userDO, userModel);

        if (userPasswordDO != null) {
            //将加密的密码设置到UserModel对象中
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }
        return userModel;
    }
}
