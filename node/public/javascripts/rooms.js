$(document).ready(function() {
//    $('.join').on('click'
    if ($('#registerlink').length) {
        $('#registerlink').on('click', function() {
            $('#registerpopup').show();
            $('#content').css('opacity', '0.5');
        });
        $('.cancel').on('click', function() {
            $('#registerpopup').hide();
            $('#content').css('opacity', 1);
        });
        $('#registerbutton').on('click', function() {
            if ($('#registerpassword').val() != $('#registerconfirm').val()) {
                $('#registeralert').text('Your passwords do not match.');
            } else {
                $('#registerloading').show();
                $.post('/register', {
                    username: $('#registerusername').val(),
                    email: $('#registeremail').val(),
                    password: $('#registerpassword').val()
                }, function(data) {
                    if (data.error) {
                        $('#registeralert').text(data.error);
                        $('#registerloading').hide();
                    } else {
                        $('#registerloading').text('Registration successful.');
                        $('#registerbutton').hide();
                        $('#registercancel').text('Close');
                    }
                });
            }
        });
    } else {
        $('#profilelink').on('click', function() {
            $('#profilepopup').show();
            $('#content').css('opacity', '0.5');
        });
        $('.cancel').on('click', function() {
            $('#profilepopup').hide();
            $('#content').css('opacity', 1);
        });
        $('#logout').on('click', function() {
            window.location.href='/logout';
        });
    }
});
