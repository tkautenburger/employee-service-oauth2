package de.legendlime.EmployeeService.repository;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import de.legendlime.EmployeeService.domain.CachedDepartment;

@Repository
public class DepartmentRedisRepositoryImpl implements DepartmentRedisRepository {

	private static final String HASH_NAME ="departments";

    private RedisTemplate<String, CachedDepartment> redisTemplate;
    private HashOperations<String, Long, CachedDepartment> hashOperations;

    public DepartmentRedisRepositoryImpl(){
        super();
    }

	@Autowired
    private DepartmentRedisRepositoryImpl(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void init() {
        hashOperations = redisTemplate.opsForHash();
    }

	@Override
	public void saveDepartment(CachedDepartment dept) {
        hashOperations.put(HASH_NAME, dept.getDepartment().getDeptId(), dept);
	}

	@Override
	public void updateDepartment(CachedDepartment dept) {
        hashOperations.put(HASH_NAME, dept.getDepartment().getDeptId(), dept);
	}

	@Override
	public void deleteDepartment(long deptId) {
        hashOperations.delete(HASH_NAME, deptId);
	}

	@Override
	public CachedDepartment findDepartment(long deptId) {
		return hashOperations.get(HASH_NAME, deptId);
	}

}
