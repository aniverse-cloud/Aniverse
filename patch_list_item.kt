<<<<<<< SEARCH
                        headlineContent = { Text(item.name, fontWeight = FontWeight.Bold) },
                        supportingContent = {
                            val subText = if (item.isRecentCall && item.date != null && item.type != null) {
                                "${item.number} • ${item.date} • ${getCallTypeString(item.type)}"
                            } else {
                                item.number
                            }
                            Text(subText)
                        },
=======
                        headlineContent = {
                            val headlineText = if (item.name.isEmpty()) item.number else item.name
                            Text(headlineText, fontWeight = FontWeight.Bold)
                        },
                        supportingContent = {
                            val subText = if (item.isRecentCall && item.date != null && item.type != null) {
                                "${item.number} • ${item.date} • ${getCallTypeString(item.type)}"
                            } else {
                                item.number
                            }
                            Text(subText)
                        },
>>>>>>> REPLACE
