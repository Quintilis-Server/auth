export interface ApiResponseType<T = null> {
    success: boolean
    message?: string
    data: T
    errorCode: ErrorCode;
    timestamp: Date;
}

interface SortType{
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
}

export interface PageResponse<T> {
    content: T[]
    empty: boolean;
    first: boolean;
    last: boolean;
    number: number;
    numberOfElements: number;
    pageable: {
        offset: number;
        pageNumber: number;
        pageSize: number;
        paged: boolean;
        sort: SortType
        unpaged:boolean;
    }
    size: number
    sort: SortType
    totalElements: number;
    totalPages: number
}

export const ErrorCode = {
    NOT_FOUND: 'NOT_FOUND',
    BAD_REQUEST: 'BAD_REQUEST',
    UNAUTHORIZED: 'UNAUTHORIZED',
    FORBIDDEN: 'FORBIDDEN',
    INTERNAL_SERVER_ERROR: 'INTERNAL_SERVER_ERROR',
    VALIDATION_ERROR: 'VALIDATION_ERROR',
    INVALID_CAPTCHA: 'INVALID_CAPTCHA',
    INVALID_TOKEN: 'INVALID_TOKEN',
    ENCRYPTION_ERROR: 'ENCRYPTION_ERROR',
    UNKNOWN_ERROR: 'UNKNOWN_ERROR',
    USER_ERROR: 'USER_ERROR',
} as const;
export type ErrorCode = typeof ErrorCode[keyof typeof ErrorCode];