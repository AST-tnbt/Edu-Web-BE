import React from 'react';
import { Avatar, Box, Button, Divider, Paper, Skeleton, Stack, TextField, Typography } from '@mui/material';
import PhotoCameraRoundedIcon from '@mui/icons-material/PhotoCameraRounded';
import type { AuthUser } from '../types/auth';
import type { UserProfile } from '../services/userService';

interface Props {
  user: AuthUser | null;
  profile: UserProfile | null;
  loading?: boolean;
}

const ProfileCard: React.FC<Props> = ({ user, profile, loading = false }) => {
  const displayName = profile?.fullName?.trim() || user?.email || 'Người dùng';
  const subtitle = user?.role ?? 'User';
  const bio = profile?.bio ?? '';
  const aboutMe = profile?.address ?? '';

  return (
    <Paper elevation={0} sx={{ p: 3, borderRadius: 4, bgcolor: 'background.paper' }}>
      <Stack spacing={3}>
        <Box>
          <Typography variant="h6" fontWeight={600} gutterBottom>
            My Profile
          </Typography>
          <Divider />
        </Box>

        <Stack direction="row" spacing={2.5} alignItems="center">
          {loading ? (
            <Skeleton variant="circular" width={72} height={72} />
          ) : (
            <Avatar
              src={profile?.avatarUrl ?? undefined}
              alt={displayName}
              sx={{ width: 72, height: 72, bgcolor: 'secondary.main', fontSize: 28 }}
            >
              {displayName.charAt(0).toUpperCase()}
            </Avatar>
          )}

          <Box>
            {loading ? (
              <>
                <Skeleton width={160} height={28} sx={{ mb: 0.5 }} />
                <Skeleton width={80} height={22} />
              </>
            ) : (
              <>
                <Typography variant="subtitle1" fontWeight={600} color="text.primary">
                  {displayName}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {subtitle}
                </Typography>
              </>
            )}
          </Box>
        </Stack>

        <Stack spacing={1.5}>
          <Typography variant="subtitle2" color="text.secondary">
            Image
          </Typography>
          <Button
            variant="outlined"
            color="primary"
            startIcon={<PhotoCameraRoundedIcon />}
            component="label"
            disabled
            sx={{ alignSelf: 'flex-start' }}
          >
            Chọn ảnh
            <input hidden type="file" />
          </Button>
        </Stack>

        <Stack spacing={2}>
          <TextField
            label="Bio"
            value={bio}
            InputProps={{ readOnly: true }}
            multiline
            minRows={4}
            fullWidth
            disabled={loading}
            placeholder="Chưa có mô tả"
          />

          <TextField
            label="About Me"
            value={aboutMe}
            InputProps={{ readOnly: true }}
            multiline
            minRows={4}
            fullWidth
            disabled={loading}
            placeholder="Chia sẻ thêm về bản thân"
          />
        </Stack>
      </Stack>
    </Paper>
  );
};

export default ProfileCard;
