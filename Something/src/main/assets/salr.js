// Copyright (c) 2009-2013 Scott Ferguson
// Copyright (c) 2013-2014 Matthew Peveler
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//     * Redistributions of source code must retain the above copyright
//       notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above copyright
//       notice, this list of conditions and the following disclaimer in the
//       documentation and/or other materials provided with the distribution.
//     * Neither the name of the software nor the
//       names of its contributors may be used to endorse or promote products
//       derived from this software without specific prior written permission.
// 
// THIS SOFTWARE IS PROVIDED BY SCOTT FERGUSON ''AS IS'' AND ANY
// EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL SCOTT FERGUSON BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

function SALR(javascriptinterface) {
    this.javascriptinterface = javascriptinterface;
    this.init();
};

SALR.prototype.init = function() {
    this.inlineTweets();
};

SALR.prototype.inlineTweets = function() {
    var that = this;
    var tweets = $('.postcontent a[href*="twitter.com"]');
    //NWS/NMS links

    //tweets = tweets.not(".postcontent:has(img[title=':nws:']) a").not(".postcontent:has(img[title=':nms:']) a");

    // spoiler'd links
    tweets = tweets.not('.bbc-spoiler a');
    tweets.each(function() {
        var match = $(this).attr('href').match(/(https|http):\/\/twitter.com\/[0-9a-zA-Z_]+\/(status|statuses)\/([0-9]+)/);
        if (match == null) {
            return;
        }
        var tweetId = match[3];
        var link = $(this);
        $.ajax({url:"https://api.twitter.com/1/statuses/oembed.json?id="+tweetId,
            dataType: 'jsonp',
            success: function(data) {
                link = $(link).wrap("<div class='tweet tw-align-center'>").parent();
                datahtml = data.html.replace("src=\"//platform.twitter.com/widgets.js\"", "src=\"file:///android_asset/twitterwidget.js\"");
                $(link).html(datahtml);
                if($('head').children('link').first().attr('href').indexOf('dark.css') != -1 || $('head').children('link').first().attr('href').indexOf('pos.css') != -1){
                    $(link).children('blockquote').first().data('theme','dark');
                }
                window.twttr.widgets.load();
            }
        });
    });
};