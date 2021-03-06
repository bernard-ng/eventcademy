package com.yvkalume.domain.usecase.user

import com.yvkalume.domain.entity.User
import com.yvkalume.domain.repository.UserRepository
import com.yvkalume.domain.util.FlowUseCase
import com.yvkalume.util.Result
import com.yvkalume.util.annotation.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddUserUseCase @Inject constructor(private val repository: UserRepository, @IoDispatcher dispatcher: CoroutineDispatcher) : FlowUseCase<User, Pair<Boolean,String>>(dispatcher) {
    override fun execute(parameters: User): Flow<Result<Pair<Boolean,String>>> {
        return repository.add(parameters)
    }
}