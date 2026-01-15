package ru.klimov.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import ru.klimov.controller.payload.BookingPayload;
import ru.klimov.dto.BookingDto;
import ru.klimov.dto.BookingResult;
import ru.klimov.dto.RoomDto;
import ru.klimov.dto.RoomReservationDto;
import ru.klimov.entity.Booking;
import ru.klimov.entity.BookingStatus;
import ru.klimov.entity.Role;
import ru.klimov.entity.User;
import ru.klimov.mapper.BookingMapper;
import ru.klimov.repository.BookingRepository;
import ru.klimov.repository.UserRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingService bookingService;

    private User user;
    private Booking booking;
    private BookingDto bookingDto;

    private void mockSecurityContext(String username, Role role) {
        Authentication authentication = mock(Authentication.class, withSettings().lenient());
        lenient().when(authentication.getName()).thenReturn(username);
        if (role != null) {
            lenient().doReturn(Collections.singletonList(new SimpleGrantedAuthority(role.name())))
                    .when(authentication).getAuthorities();
        }
        SecurityContext securityContext = mock(SecurityContext.class, withSettings().lenient());
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");

        booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setRequestId(UUID.randomUUID());
        booking.setUser(user);
        booking.setRoomId(UUID.randomUUID());
        booking.setStartDate(LocalDate.now().plusDays(1));
        booking.setEndDate(LocalDate.now().plusDays(5));
        booking.setStatus(BookingStatus.PENDING);

        bookingDto = BookingDto.builder()
                .id(booking.getId())
                .roomId(booking.getRoomId())
                .status(booking.getStatus())
                .build();
    }

    @Test
    void findAll_ShouldReturnListOfDtos() {
        // given
        when(bookingRepository.findAll()).thenReturn(Collections.singletonList(booking));
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        // when
        Iterable<BookingDto> result = bookingService.findAll();

        // then
        assertThat(result).containsExactly(bookingDto);
    }

    @Test
    void findAllByUsername_AsAdmin_ShouldReturnListOfDtos() {
        // given
        String username = "otheruser";
        mockSecurityContext("admin", Role.ADMIN);
        when(bookingRepository.findAllByUserUsername(username)).thenReturn(Collections.singletonList(booking));
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        // when
        Iterable<BookingDto> result = bookingService.findAllByUsername(username);

        // then
        assertThat(result).containsExactly(bookingDto);
    }

    @Test
    void findAllByUsername_AsOwner_ShouldReturnListOfDtos() {
        // given
        String username = "testuser";
        mockSecurityContext(username, Role.USER);
        when(bookingRepository.findAllByUserUsername(username)).thenReturn(Collections.singletonList(booking));
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        // when
        Iterable<BookingDto> result = bookingService.findAllByUsername(username);

        // then
        assertThat(result).containsExactly(bookingDto);
    }

    @Test
    void findAllByUsername_AsOtherUser_ShouldThrowAccessDeniedException() {
        // given
        String username = "otheruser";
        mockSecurityContext("testuser", Role.USER);

        // when & then
        assertThatThrownBy(() -> bookingService.findAllByUsername(username))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void cancelBooking_AsAdmin_ShouldCancelAndReleaseRoom() {
        // given
        UUID requestId = booking.getRequestId();
        mockSecurityContext("admin", Role.ADMIN);
        when(bookingRepository.findByRequestId(requestId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        // when
        BookingDto result = bookingService.cancelBooking(requestId);

        // then
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(restTemplate).postForObject(eq("http://hotel-service/rooms/{id}/release/{requestId}"), isNull(), eq(Void.class), eq(booking.getRoomId()), eq(requestId));
        verify(bookingRepository).save(booking);
    }

    @Test
    void cancelBooking_AsOwner_ShouldCancelAndReleaseRoom() {
        // given
        UUID requestId = booking.getRequestId();
        mockSecurityContext("testuser", Role.USER);
        when(bookingRepository.findByRequestId(requestId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        // when
        BookingDto result = bookingService.cancelBooking(requestId);

        // then
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(restTemplate).postForObject(eq("http://hotel-service/rooms/{id}/release/{requestId}"), isNull(), eq(Void.class), eq(booking.getRoomId()), eq(requestId));
        verify(bookingRepository).save(booking);
    }

    @Test
    void cancelBooking_AsOtherUser_ShouldCancelAndReleaseRoom() {
        // given
        UUID requestId = booking.getRequestId();
        mockSecurityContext("otheruser", Role.USER);
        when(bookingRepository.findByRequestId(requestId)).thenReturn(Optional.of(booking));

        // when & then
        assertThatThrownBy(() -> bookingService.cancelBooking(requestId))
                .isInstanceOf(AccessDeniedException.class);;
    }

    @Test
    void cancelBooking_NotFound_ShouldThrowException() {
        // given
        UUID requestId = UUID.randomUUID();
        when(bookingRepository.findByRequestId(requestId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bookingService.cancelBooking(requestId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Booking not found");
    }

    @Test
    void create_SuccessfulWithExplicitRoomId() {
        // given
        mockSecurityContext(user.getUsername(), Role.USER);
        BookingPayload payload = new BookingPayload();
        payload.setRoomId(booking.getRoomId());
        payload.setStartDate(booking.getStartDate());
        payload.setEndDate(booking.getEndDate());
        payload.setAutoSelect(false);

        RoomReservationDto reservationDto = new RoomReservationDto();

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toRoomReservationDto(any(Booking.class))).thenReturn(reservationDto);
        when(restTemplate.postForObject(eq("http://hotel-service/rooms/{id}/confirm-availability"), eq(reservationDto), eq(Boolean.class), eq(booking.getRoomId())))
                .thenReturn(true);
        when(bookingMapper.toBookingResultDto(booking)).thenReturn(BookingResult.builder().success(true).build());

        // when
        BookingResult result = bookingService.create(payload);

        // then
        assertThat(result.getSuccess()).isTrue();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(bookingRepository, times(2)).save(any(Booking.class));
    }

    @Test
    void create_RoomNotAvailable_ShouldCancelBooking() {
        // given
        mockSecurityContext(user.getUsername(), Role.USER);
        BookingPayload payload = new BookingPayload();
        payload.setRoomId(booking.getRoomId());
        payload.setStartDate(booking.getStartDate());
        payload.setEndDate(booking.getEndDate());
        payload.setAutoSelect(false);

        RoomReservationDto reservationDto = new RoomReservationDto();

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toRoomReservationDto(any(Booking.class))).thenReturn(reservationDto);
        when(restTemplate.postForObject(anyString(), any(), eq(Boolean.class), any(UUID.class)))
                .thenReturn(false);

        // when
        BookingResult result = bookingService.create(payload);

        // then
        assertThat(result.getSuccess()).isFalse();
        assertThat(result.getMessage()).isEqualTo("Room is not available");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void create_WithAutoSelect_ShouldUseRecommendedRoom() {
        // given
        mockSecurityContext(user.getUsername(), Role.USER);
        BookingPayload payload = new BookingPayload();
        payload.setStartDate(booking.getStartDate());
        payload.setEndDate(booking.getEndDate());
        payload.setAutoSelect(true);

        RoomDto recommendedRoom = RoomDto.builder().id(UUID.randomUUID()).build();
        RoomDto[] roomsArray = new RoomDto[]{recommendedRoom};

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(restTemplate.getForObject("http://hotel-service/rooms/recommend", RoomDto[].class)).thenReturn(roomsArray);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(restTemplate.postForObject(anyString(), any(), eq(Boolean.class), any(UUID.class))).thenReturn(true);
        when(bookingMapper.toBookingResultDto(any())).thenReturn(BookingResult.builder().success(true).build());

        // when
        bookingService.create(payload);

        // then
        verify(restTemplate).getForObject("http://hotel-service/rooms/recommend", RoomDto[].class);
    }

    @Test
    void create_UserNotFound_ShouldThrowException() {
        // given
        mockSecurityContext("nonexistentuser", Role.USER);
        BookingPayload payload = new BookingPayload();
        payload.setStartDate(LocalDate.parse("2025-11-10"));
        payload.setEndDate(LocalDate.parse("2025-11-15"));


        when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bookingService.create(payload))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getOffers_ShouldReturnListOfRoomDtos() {
        // given
        RoomDto room = RoomDto.builder().id(UUID.randomUUID()).build();
        when(restTemplate.getForObject("http://hotel-service/rooms/recommend", RoomDto[].class))
                .thenReturn(new RoomDto[]{room});

        // when
        List<RoomDto> result = bookingService.getOffers();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(room.getId());
    }

    @Test
    void findByRequestId_ShouldReturnDto() {
        // given
        UUID requestId = UUID.randomUUID();
        when(bookingRepository.findByRequestId(requestId)).thenReturn(Optional.of(booking));
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        // when
        BookingDto result = bookingService.findByRequestId(requestId);

        // then
        assertThat(result).isEqualTo(bookingDto);
    }
}
