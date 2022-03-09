package com.ick.kalambury.net.api.dto

import com.ick.kalambury.TableKind
import kotlinx.serialization.Serializable

@Serializable
data class TablesDto(val tables: Map<TableKind, List<TableDto>>)