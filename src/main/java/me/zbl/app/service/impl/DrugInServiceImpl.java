/*
 * Copyright 2018 JamesZBL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package me.zbl.app.service.impl;

import me.zbl.app.dao.DrugMapper;
import me.zbl.app.dao.ExpireMapper;
import me.zbl.app.dao.InventoryMapper;
import me.zbl.app.domain.DrugInDO;
import me.zbl.app.domain.DrugInFormDO;
import me.zbl.app.domain.Expire;
import me.zbl.app.service.DrugInService;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 药品入库业务实现
 *
 * @author JamesZBL
 * @email 1146556298@qq.com
 * @date 2018-05-07
 */
@Service
public class DrugInServiceImpl implements DrugInService {

  @Autowired
  private ExpireMapper expireMapper;

  @Autowired
  private InventoryMapper inventoryMapper;

  @Autowired
  private DrugMapper drugMapper;

  @Override
  public List<DrugInDO> list(Map<String, Object> params) {
    return inventoryMapper.inList(params);
  }

  @Override
  public int count() {
    return inventoryMapper.countIn();
  }

  @Override
  public int drugInSave(DrugInFormDO drugInFormDO) {
    // 生产日期
    Date madeDate = drugInFormDO.getMadeDate();
    // 保质期
    int guarantee = drugMapper.selectByPrimaryKey(drugInFormDO.getDrugId()).getQualityGuaranteePeriod();
    // 计算过期日期
    Date expire = DateUtils.addMonths(madeDate, guarantee);
    Expire exp = new Expire();
    exp.setDrugId(drugInFormDO.getDrugId());
    exp.setExpiredDate(expire);
    expireMapper.insertSelective(exp);
    // 库存记录
    Map<String, Object> params = new HashMap<>();
    params.put("drugId", drugInFormDO.getDrugId());
    params.put("quantity", drugInFormDO.getQuantity());
    // 更新药品的库存
    drugMapper.increaseAndDecreaseQuantity(params);
    // 保存仓储变动记录
    return inventoryMapper.drugInSave(drugInFormDO);
  }
}
