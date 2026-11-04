package com.it_goes.api.dto;

import java.util.List;

public record TripPageDto(List<TripSummaryDto> trips, int numPages) {
}
