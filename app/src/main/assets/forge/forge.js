// Lia AI - 3D Website Forge Scene Engine
(function() {
  var canvas = document.getElementById('forgeCanvas');
  var ctx = canvas.getContext('2d');
  var width, height;
  var progress = 0.05;
  var stageName = "Initializing";
  var particles = [];
  var rings = [];
  var isRevealing = false;
  var revealProgress = 0;
  var animationFrameId = null;

  var colors = {
    bg: '#06050E',
    core: '#FFB547',
    glow: '#FF8A3D',
    rim: '#FF6FAE',
    cyan: '#3FE0D0'
  };

  function resize() {
    width = canvas.width = window.innerWidth * (window.devicePixelRatio || 1);
    height = canvas.height = window.innerHeight * (window.devicePixelRatio || 1);
  }

  function initParticles() {
    particles = [];
    var count = 120;
    for (var i = 0; i < count; i++) {
      particles.push({
        x: (Math.random() - 0.5) * 500,
        y: (Math.random() - 0.5) * 500,
        z: Math.random() * 500,
        radius: Math.random() * 2 + 1,
        speed: Math.random() * 2 + 1,
        angle: Math.random() * Math.PI * 2
      });
    }
  }

  function render(time) {
    if (!ctx) return;
    ctx.clearRect(0, 0, width, height);

    var cx = width / 2;
    var cy = height / 2;
    var baseRadius = Math.min(width, height) * 0.18 * (1 + Math.sin(time * 0.003) * 0.05);

    // Background gradient aura
    var grad = ctx.createRadialGradient(cx, cy, baseRadius * 0.2, cx, cy, baseRadius * 2.5);
    grad.addColorStop(0, 'rgba(255, 181, 71, 0.25)');
    grad.addColorStop(0.5, 'rgba(255, 110, 174, 0.12)');
    grad.addColorStop(1, 'rgba(6, 5, 14, 0)');
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, width, height);

    // Floor grid lines
    ctx.strokeStyle = 'rgba(63, 224, 208, 0.12)';
    ctx.lineWidth = 1;
    var gridSpacing = 40;
    for (var x = 0; x < width; x += gridSpacing) {
      ctx.beginPath();
      ctx.moveTo(x, cy + baseRadius);
      ctx.lineTo((x - cx) * 2 + cx, height);
      ctx.stroke();
    }

    // Rotating gyro rings
    ctx.save();
    ctx.translate(cx, cy);
    var ringCount = 3;
    for (var r = 0; r < ringCount; r++) {
      ctx.save();
      var rot = time * 0.001 * (r + 1) * (r % 2 === 0 ? 1 : -1);
      ctx.rotate(rot);
      ctx.beginPath();
      ctx.ellipse(0, 0, baseRadius * (1.2 + r * 0.2), baseRadius * 0.5, 0, 0, Math.PI * 2);
      ctx.strokeStyle = r === 0 ? colors.core : (r === 1 ? colors.rim : colors.cyan);
      ctx.lineWidth = 2.5;
      ctx.globalAlpha = 0.6 + Math.sin(time * 0.004 + r) * 0.3;
      ctx.stroke();
      ctx.restore();
    }

    // Swirling energy particles
    for (var p = 0; p < particles.length; p++) {
      var pt = particles[p];
      pt.angle += 0.02 * pt.speed;
      var dist = baseRadius * (1.1 + (p % 4) * 0.25);
      var px = Math.cos(pt.angle) * dist;
      var py = Math.sin(pt.angle) * dist * 0.6;

      ctx.beginPath();
      ctx.arc(px, py, pt.radius * (1 + progress * 0.8), 0, Math.PI * 2);
      ctx.fillStyle = (p % 2 === 0) ? colors.core : colors.cyan;
      ctx.globalAlpha = 0.8;
      ctx.fill();
    }

    // Core Reactor
    var coreGrad = ctx.createRadialGradient(0, 0, baseRadius * 0.1, 0, 0, baseRadius);
    coreGrad.addColorStop(0, '#FFFFFF');
    coreGrad.addColorStop(0.3, colors.core);
    coreGrad.addColorStop(0.8, colors.glow);
    coreGrad.addColorStop(1, 'rgba(255, 110, 174, 0)');
    ctx.beginPath();
    ctx.arc(0, 0, baseRadius * (0.8 + progress * 0.3), 0, Math.PI * 2);
    ctx.fillStyle = coreGrad;
    ctx.globalAlpha = 0.95;
    ctx.fill();
    ctx.restore();

    // Reveal flash transition
    if (isRevealing) {
      revealProgress += 0.025;
      ctx.fillStyle = 'rgba(255, 255, 255, ' + Math.min(1, revealProgress) + ')';
      ctx.fillRect(0, 0, width, height);

      if (revealProgress >= 1.2) {
        isRevealing = false;
        if (window.ForgeBridge && window.ForgeBridge.onRevealFinished) {
          window.ForgeBridge.onRevealFinished();
        }
      }
    }

    animationFrameId = requestAnimationFrame(render);
  }

  window.Forge = {
    init: function(theme) {
      window.addEventListener('resize', resize);
      resize();
      initParticles();
      if (theme && theme.accent) {
        colors.core = theme.accent;
      }
      animationFrameId = requestAnimationFrame(render);
      if (window.ForgeBridge && window.ForgeBridge.onReady) {
        window.ForgeBridge.onReady();
      }
    },
    setStage: function(idx, name) {
      stageName = name || "";
      var chip = document.getElementById('statusChip');
      if (chip) chip.innerText = "Stage: " + stageName;
    },
    setProgress: function(p) {
      progress = Math.max(0, Math.min(1, p));
    },
    reveal: function() {
      isRevealing = true;
      revealProgress = 0;
    },
    dispose: function() {
      if (animationFrameId) {
        cancelAnimationFrame(animationFrameId);
      }
      window.removeEventListener('resize', resize);
    }
  };

  window.addEventListener('DOMContentLoaded', function() {
    window.Forge.init({});
  });
})();
