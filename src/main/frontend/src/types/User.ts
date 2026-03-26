export interface User {
    id: string;
    username: string;
    roles: string[];
    avatarPath?: string;
    email?: string;
    isVerified: boolean;
}
export interface UserSummaryDTO {
    id: string
    username: string
    roles: string[]
    avatarPath: string | null
    isVerified: boolean
}