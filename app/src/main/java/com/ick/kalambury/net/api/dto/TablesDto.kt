package com.ick.kalambury.net.api.dto

import androidx.annotation.Keep
import com.ick.kalambury.TableKind

@Keep
data class TablesDto(val tables: Map<TableKind, List<TableDto>> = mapOf())