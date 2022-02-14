package com.ick.kalambury.service

import com.ick.kalambury.list.model.Player

fun one(player: Player) = listOf(player.id)

val Map<String, Player>.all
    get() = values.filter(Player::active).map(Player::id)

fun Map<String, Player>.allExcept(player: Player) = values
    .filter { p -> p.active && p.uuid != player.uuid }
    .map(Player::id)