-- 1.参数列表
-- 1.1. 优惠券ID
local voucherId = ARGV[1]
-- 1.2. 用户ID
local userId = ARGV[2]
-- 1.3 订单ID
local orderId = ARGV[3]

-- 2.数据key
-- 2.1. 库存key
local stockKey = 'seckill:stock:' .. voucherId
-- 2.2. 订单key
local orderKey = 'seckill:order:' .. voucherId

-- 3. 脚本业务
-- 3.1 判断库存是否充足
local stock = redis.call('get', stockKey)
if(stock == nil or tonumber(stock) <= 0) then
    return 1
end

-- 3.2. 判断用户是否下单
if(redis.call('sismember', orderKey, userId) > 0) then
    -- 3.3. 存在，说明重复下单
    return 2
end
-- 3.4.扣库存
redis.call('incrby', stockKey, -1)
-- 3.5. 下单（保存用户）
redis.call('sadd', orderKey, userId)
-- 3.6. 发送消息到消息到消息队列，XADD stream.orders * k1 v1 k2 v2
redis.call('xadd','stream.orders','*', 'userId', userId, 'voucherId', voucherId, 'id', orderId)
return 0