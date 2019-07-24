package pw.aru.db.entities.scoped.user

import java.time.LocalDate

/*
 * UserProfile
 *  - id BIGINT PRIMARY_KEY # discord user snowflake
 *  - birthday DATE
 *  - money BIGINT
 *  - premiumCredits BIGINT
 *  - reputation BIGINT
 *  - experience BIGINT
 *  - level BIGINT
 */
data class UserProfile(
    val id: Long,
    val birthday: LocalDate? = null,
    val money: Long = 0,
    val premiumCredits: Long = 0,

    val reputation: Long = 0,
    val experience: Long = 0,
    val level: Long = 0
)

